package br.com.matheus.orcafacil.openapi;

import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerRequest;
import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerResponse;
import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerUpdateRequest;
import br.com.matheus.orcafacil.shared.web.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

/**
 * Documentação OpenAPI de {@link br.com.matheus.orcafacil.domain.customer.web.CustomerController}.
 */
@Tag(name = "Clientes", description = "Clientes do negócio. Clientes não são excluídos, apenas desativados.")
public interface CustomerApiDocs {

    @Operation(
            summary = "Lista os clientes",
            description = "Por padrão retorna os clientes ativos; com deactivated=true, apenas os desativados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes ordenada por nome")
    })
    List<CustomerResponse> findAll(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "true para listar apenas os clientes desativados") boolean deactivated
    );

    @Operation(
            summary = "Busca um cliente pelo id",
            description = "Retorna o cliente mesmo que esteja desativado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    CustomerResponse findById(@Parameter(hidden = true) Jwt jwt, UUID customerId);

    @Operation(summary = "Cadastra um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ainda não cadastrou o negócio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um cliente com este CPF/CNPJ no negócio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    CustomerResponse create(@Parameter(hidden = true) Jwt jwt, CustomerRequest request);

    @Operation(
            summary = "Atualiza um cliente ativo",
            description = "Atualização parcial: campos ausentes ou nulos mantêm o valor atual. Clientes desativados não podem ser editados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente ativo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um cliente com este CPF/CNPJ no negócio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    CustomerResponse update(@Parameter(hidden = true) Jwt jwt, UUID customerId, CustomerUpdateRequest request);

    @Operation(
            summary = "Desativa um cliente",
            description = "Clientes desativados não podem ser usados em novos orçamentos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente desativado"),
            @ApiResponse(responseCode = "404", description = "Cliente ativo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    CustomerResponse deactivate(@Parameter(hidden = true) Jwt jwt, UUID customerId);

    @Operation(summary = "Reativa um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente reativado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    CustomerResponse reactivate(@Parameter(hidden = true) Jwt jwt, UUID customerId);
}
