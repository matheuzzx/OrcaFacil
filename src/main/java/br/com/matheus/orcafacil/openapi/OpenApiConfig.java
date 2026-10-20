package br.com.matheus.orcafacil.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI (Swagger UI em /swagger-ui.html).
 * Todas as rotas exigem o access token do Supabase Auth.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Orça Fácil API",
                version = "v1",
                description = "API para autônomos e pequenos negócios criarem e gerenciarem orçamentos. "
                        + "Todos os dados são isolados pelo usuário dono do token."
        ),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Access token do Supabase Auth (campo access_token da sessão). "
                + "Sem token ou com token inválido, a API responde 401."
)
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";
}
