package br.com.matheus.orcafacil.domain.quote.web;

import br.com.matheus.orcafacil.domain.quote.service.QuoteDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Endpoints que devolvem o orçamento como documento (não JSON).
 * Hoje: preview em HTML. Depois: o mesmo HTML convertido em PDF.
 */
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteViewController {

    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

    private final QuoteDocumentService quoteDocumentService;

    // Sem "produces = text/html" de propósito: se o orçamento não existir, o
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

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
