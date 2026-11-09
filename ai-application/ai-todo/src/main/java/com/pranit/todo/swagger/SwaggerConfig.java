
package com.pranit.todo.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TODO Application",
                description = "TODO Application REST API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Pranit Bhangale",
                        url = "https://pranitbhangale.vercel.app",
                        email = "pranitbhangale1453@gmail.com"
                )
        ),
        security = {
                @SecurityRequirement(name = "accessToken")
        }
)
@SecurityScheme(
        name = "accessToken",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter your JWT access token"
)
public class SwaggerConfig {
}
