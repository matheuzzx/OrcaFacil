package br.com.matheus.orcafacil.openapi;

import br.com.matheus.orcafacil.shared.web.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Documentação OpenAPI de {@link br.com.matheus.orcafacil.domain.quote.web.QuoteViewController}.
 */
@Tag(name = "Orçamentos")
public interface QuoteViewApiDocs {

    @Operation(summary = "Gera o preview do orçamento em HTML")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento HTML do orçamento",
                    content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<String> preview(@Parameter(hidden = true) Jwt jwt, UUID quoteId);

    @Operation(
            summary = "Gera o PDF do orçamento",
            description = "Por padrão o PDF abre no navegador (inline); com download=true, é baixado como anexo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo PDF do orçamento",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<byte[]> pdf(
            @Parameter(hidden = true) Jwt jwt,
            UUID quoteId,
            @Parameter(description = "true para baixar o arquivo em vez de abri-lo no navegador") boolean download
    );
}
