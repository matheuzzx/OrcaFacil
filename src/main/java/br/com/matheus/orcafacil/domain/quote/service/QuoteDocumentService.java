package br.com.matheus.orcafacil.domain.quote.service;

import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto;
import br.com.matheus.orcafacil.shared.format.BrazilianFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

/**
 * Gera o documento do orçamento a partir do template Thymeleaf.
 *
 * <p>O HTML é renderizado para uma String (e não como "view" do Spring MVC)
 * justamente para ser reaproveitado: o preview devolve essa String ao navegador
 * e o futuro {@code renderPdf} vai entregar a mesma String ao openhtmltopdf.
 */
@Service
@RequiredArgsConstructor
public class QuoteDocumentService {

    static final String TEMPLATE = "quote-pdf";

    private final QuoteService quoteService;
    private final TemplateEngine templateEngine;

    /**
     * Transacional porque o mapeamento lê business, customer e items, que são LAZY.
     * O findById já filtra pelo dono, então um usuário nunca vê orçamento de outro (404).
     */
    @Transactional(readOnly = true)
    public String renderHtml(UUID ownerId, UUID quoteId) {
        Quote quote = quoteService.findById(ownerId, quoteId);
        return renderHtml(QuoteViewDto.from(quote));
    }

    public String renderHtml(QuoteViewDto view) {
        Context context = new Context(BrazilianFormat.PT_BR);
        context.setVariable("quote", view);
        return templateEngine.process(TEMPLATE, context);
    }

    // Próximo passo (PDF), reaproveitando o mesmo HTML:
    // public byte[] renderPdf(UUID ownerId, UUID quoteId) {
    //     String html = renderHtml(ownerId, quoteId);
    //     ByteArrayOutputStream out = new ByteArrayOutputStream();
    //     new PdfRendererBuilder().withHtmlContent(html, null).toStream(out).run();
    //     return out.toByteArray();
    // }
}
