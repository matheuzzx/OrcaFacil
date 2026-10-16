package br.com.matheus.orcafacil.domain.quote.web;

import br.com.matheus.orcafacil.domain.quote.service.QuoteDocumentService;
import br.com.matheus.orcafacil.domain.quote.service.QuoteDocumentService.PdfDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Endpoints que devolvem o orçamento como documento (não JSON):
 * preview em HTML e o mesmo conteúdo em PDF.
 */
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteViewController {

    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

    private final QuoteDocumentService quoteDocumentService;

    // Sem "produces" de propósito nas duas rotas: se o orçamento não existir, o
    // GlobalExceptionHandler precisa conseguir responder o 404 em JSON.
    @GetMapping("/{quoteId}/preview")
    public ResponseEntity<String> preview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId
    ) {
        String html = quoteDocumentService.renderHtml(ownerId(jwt), quoteId);

        return ResponseEntity.ok()
                // charset explícito: sem ele a String sai em ISO-8859-1 e quebra os acentos
                .contentType(HTML_UTF8)
                // o orçamento muda a cada edição: o preview nunca deve vir do cache
                .cacheControl(CacheControl.noStore())
                .body(html);
    }

    /**
     * Por padrão abre o PDF no navegador (inline). Com {@code ?download=true}
     * o navegador baixa o arquivo.
     */
    @GetMapping("/{quoteId}/pdf")
    public ResponseEntity<byte[]> pdf(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @RequestParam(defaultValue = "false") boolean download
    ) {
        PdfDocument pdf = quoteDocumentService.renderPdf(ownerId(jwt), quoteId);

        ContentDisposition disposition = (download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(pdf.filename())
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .body(pdf.content());
    }

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
