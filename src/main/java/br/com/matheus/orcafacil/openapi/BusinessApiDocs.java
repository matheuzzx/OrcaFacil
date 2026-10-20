package br.com.matheus.orcafacil.openapi;

import br.com.matheus.orcafacil.domain.business.web.dto.BusinessRequest;
import br.com.matheus.orcafacil.domain.business.web.dto.BusinessResponse;
import br.com.matheus.orcafacil.domain.business.web.dto.BusinessUpdateRequest;
import br.com.matheus.orcafacil.shared.web.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Documentação OpenAPI de {@link br.com.matheus.orcafacil.domain.business.web.BusinessController}.
 */
@Tag(name = "Negócio", description = "Dados comerciais de quem emite os orçamentos. Cada usuário tem um único negócio.")
public interface BusinessApiDocs {

    @Operation(summary = "Busca o negócio do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Negócio encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário ainda não cadastrou o negócio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    BusinessResponse findCurrentBusiness(@Parameter(hidden = true) Jwt jwt);

    @Operation(summary = "Cadastra o negócio do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Negócio cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Usuário já possui um negócio cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    BusinessResponse create(@Parameter(hidden = true) Jwt jwt, BusinessRequest request);

    @Operation(
            summary = "Atualiza o negócio do usuário autenticado",
            description = "Atualização parcial: campos ausentes ou nulos mantêm o valor atual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Negócio atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ainda não cadastrou o negócio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    BusinessResponse update(@Parameter(hidden = true) Jwt jwt, BusinessUpdateRequest request);
}
