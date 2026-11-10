package com.pranit.todo.chat.advisor;

import com.pranit.todo.chat.event.ToolCallEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Sits INSIDE ToolCallingAdvisor's recursive loop (order is greater than
 * {@link ToolCallingAdvisor#DEFAULT_ORDER}, i.e. closer to the model), so it
 * sees every intermediate chunk that the tool loop normally hides from the
 * outbound stream — including the assistant chunks that carry tool calls.
 * <p>
 * Two observation points:
 * <ul>
 *   <li>STARTED — a streamed chunk's generation carries a tool call. Arguments
 *       arrive as partial JSON fragments across chunks, so we de-dupe on the
 *       call id and only report the name.</li>
 *   <li>COMPLETED — on the next loop iteration, the incoming request's message
 *       history contains the ToolResponseMessage(s) from the previous round.
 *       Matching response ids against previously started calls tells us those
 *       tools finished.</li>
 * </ul>
 * <p>
 * Create one instance per request: it keeps a per-conversation id state.
 */
public class ToolCallObservingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * Inside the tool loop: ToolCallingAdvisor.DEFAULT_ORDER is MIN_VALUE + 300.
     */
    public static final int ORDER = ToolCallingAdvisor.DEFAULT_ORDER + 100;

    private final Consumer<ToolCallEvent> listener;
    private final Set<String> started = ConcurrentHashMap.newKeySet();
    private final Set<String> completed = ConcurrentHashMap.newKeySet();

    public ToolCallObservingAdvisor(Consumer<ToolCallEvent> listener) {
        this.listener = listener;
    }

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, StreamAdvisorChain chain) {
        emitCompletions(request);
        return chain.nextStream(request).doOnNext(this::emitStarts);
    }

    private void emitCompletions(ChatClientRequest request) {
        request.prompt().getInstructions().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(message -> message.getResponses().stream())
                .filter(response -> {
                    String id = StringUtils.hasText(response.id()) ? response.id() : response.name();
                    return (this.started.contains(id) || this.started.contains(response.name()) || this.started.isEmpty())
                            && this.completed.add(id);
                })
                .forEach(response -> {
                    String id = StringUtils.hasText(response.id()) ? response.id() : response.name();
                    this.listener.accept(new ToolCallEvent(id, response.name(), ToolCallEvent.Phase.COMPLETED, response.responseData()));
                });
    }

    private void emitStarts(ChatClientResponse response) {
        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse == null) return;
        chatResponse.getResults().stream()
                .flatMap(generation -> generation.getOutput().getToolCalls().stream())
                .filter(toolCall -> StringUtils.hasText(toolCall.name()))
                .filter(toolCall -> {
                    String id = StringUtils.hasText(toolCall.id()) ? toolCall.id() : toolCall.name();
                    return this.started.add(id);
                })
                .forEach(toolCall -> {
                    String id = StringUtils.hasText(toolCall.id()) ? toolCall.id() : toolCall.name();
                    this.listener.accept(new ToolCallEvent(id, toolCall.name(), ToolCallEvent.Phase.STARTED));
                });
    }

    @Override
    public @NonNull ChatClientResponse adviseCall(@NonNull ChatClientRequest request, CallAdvisorChain chain) {
        emitCompletions(request);
        ChatClientResponse response = chain.nextCall(request);
        emitStarts(response);
        return response;
    }

    @Override
    public @NonNull String getName() {
        return "toolCallObservingAdvisor";
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
