package br.com.matheus.orcafacil.openapi;

import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemReorderRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemUpdateRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteResponse;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteStatusRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteUpdateRequest;
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
 * Documentação OpenAPI de {@link br.com.matheus.orcafacil.domain.quote.web.QuoteController}.
 */
@Tag(name = "Orçamentos", description = "Orçamentos e seus itens. Só orçamentos em rascunho (DRAFT) podem ser alterados.")
public interface QuoteApiDocs {

    @Operation(summary = "Lista os orçamentos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de orçamentos, dos mais recentes para os mais antigos"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    List<QuoteResponse> findAll(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "Filtra pelo status; sem o parâmetro, retorna todos") QuoteStatus status
    );

    @Operation(summary = "Busca um orçamento pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento com seus itens"),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse findById(@Parameter(hidden = true) Jwt jwt, UUID quoteId);

    @Operation(
            summary = "Cria um orçamento",
            description = "O orçamento nasce como DRAFT, com número sequencial por negócio. Sem issueDate, a emissão é a data atual. Os itens são opcionais e podem ser adicionados depois."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orçamento criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Negócio não cadastrado ou cliente ativo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Validade anterior à emissão ou desconto maior que o subtotal",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse create(@Parameter(hidden = true) Jwt jwt, QuoteRequest request);

    @Operation(
            summary = "Atualiza os dados de um orçamento",
            description = "Somente em DRAFT. Atualização parcial: campos ausentes ou nulos mantêm o valor atual. Subtotal e total são recalculados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orçamento ou cliente ativo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho, validade anterior à emissão ou desconto maior que o subtotal",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse update(@Parameter(hidden = true) Jwt jwt, UUID quoteId, QuoteUpdateRequest request);

    @Operation(
            summary = "Exclui um orçamento em rascunho",
            description = "Exclusão definitiva, permitida apenas em DRAFT. Depois disso, use o status CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orçamento excluído"),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    void delete(@Parameter(hidden = true) Jwt jwt, UUID quoteId);

    @Operation(
            summary = "Altera o status de um orçamento",
            description = "Transições permitidas: DRAFT → SENT ou CANCELLED; SENT → APPROVED, REJECTED, EXPIRED ou CANCELLED. APPROVED, REJECTED, EXPIRED e CANCELLED são finais. Para enviar (SENT), o orçamento precisa ter ao menos um item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado (ou já era o status informado)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transição de status não permitida ou orçamento sem itens ao enviar",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse changeStatus(@Parameter(hidden = true) Jwt jwt, UUID quoteId, QuoteStatusRequest request);

    @Operation(
            summary = "Adiciona um item ao orçamento",
            description = "Somente em DRAFT. O item entra no fim da lista; total do item e do orçamento são recalculados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item adicionado; retorna o orçamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse addItem(@Parameter(hidden = true) Jwt jwt, UUID quoteId, QuoteItemRequest request);

    @Operation(
            summary = "Reordena os itens do orçamento",
            description = "Somente em DRAFT. itemIds deve conter todos os itens atuais, cada um uma única vez, na ordem desejada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens reordenados; retorna o orçamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho ou itemIds não corresponde exatamente aos itens atuais",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse reorderItems(@Parameter(hidden = true) Jwt jwt, UUID quoteId, QuoteItemReorderRequest request);

    @Operation(
            summary = "Atualiza um item do orçamento",
            description = "Somente em DRAFT. Atualização parcial: campos ausentes ou nulos mantêm o valor atual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado; retorna o orçamento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (VALIDATION_ERROR ou MALFORMED_REQUEST)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orçamento ou item não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    QuoteResponse updateItem(@Parameter(hidden = true) Jwt jwt, UUID quoteId, UUID itemId, QuoteItemUpdateRequest request);

    @Operation(
            summary = "Remove um item do orçamento",
            description = "Somente em DRAFT. As posições dos itens restantes e os totais são recalculados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido"),
            @ApiResponse(responseCode = "404", description = "Orçamento ou item não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Orçamento não está em rascunho",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    void removeItem(@Parameter(hidden = true) Jwt jwt, UUID quoteId, UUID itemId);
}
