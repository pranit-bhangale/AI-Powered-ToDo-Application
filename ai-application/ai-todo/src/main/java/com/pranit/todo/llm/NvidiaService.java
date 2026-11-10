package com.pranit.todo.llm;

import com.pranit.todo.chat.stream.ChatEventStream;
import com.pranit.todo.tools.TaskTools;
import com.pranit.todo.tools.TodoTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class NvidiaService implements AiService {

    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;
    private final TodoTools todoTools;
    private final TaskTools taskTools;

    @Override
    public Flux<ServerSentEvent<String>> askAiAgent(final String prompt, final String conversationId) {
        final ChatEventStream events = new ChatEventStream(this.jsonMapper);
        final Flux<ServerSentEvent<String>> answerTokens = this.chatClient.prompt()
                .advisors(events.observer())
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(this.todoTools, this.taskTools)
                .user(prompt)
                .stream()
                .content()
                .map(events::token);
        return events.mergeWith(answerTokens);
    }
}
