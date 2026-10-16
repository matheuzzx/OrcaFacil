package br.com.matheus.orcafacil.domain.quote.service;

import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto;
import br.com.matheus.orcafacil.shared.format.BrazilianFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * Gera o documento do orçamento a partir do template Thymeleaf.
 *
 * <p>O HTML é renderizado para uma String (e não como "view" do Spring MVC)
 * justamente para ser reaproveitado: o preview devolve essa String ao navegador
 * e o PDF entrega a mesma String ao openhtmltopdf. Os dois sempre mostram o
 * mesmo conteúdo.
 */
@Service
@RequiredArgsConstructor
public class QuoteDocumentService {

    static final String TEMPLATE = "quote-pdf";

    private final QuoteService quoteService;
    private final TemplateEngine templateEngine;

    /** PDF pronto para download, com o nome sugerido para o arquivo. */
    public record PdfDocument(String filename, byte[] content) {
    }

    /**
     * Transacional porque o mapeamento lê business, customer e items, que são LAZY.
     * O findById já filtra pelo dono, então um usuário nunca vê orçamento de outro (404).
     */
    @Transactional(readOnly = true)
    public String renderHtml(UUID ownerId, UUID quoteId) {
        return renderHtml(viewOf(ownerId, quoteId));
    }

    public String renderHtml(QuoteViewDto view) {
        Context context = new Context(BrazilianFormat.PT_BR);
        context.setVariable("quote", view);
        return templateEngine.process(TEMPLATE, context);
    }

    /** Mesma regra de acesso do preview: só o dono do orçamento gera o PDF. */
    @Transactional(readOnly = true)
    public PdfDocument renderPdf(UUID ownerId, UUID quoteId) {
        QuoteViewDto view = viewOf(ownerId, quoteId);
        return new PdfDocument("orcamento-" + view.number() + ".pdf", renderPdf(view));
    }

    public byte[] renderPdf(QuoteViewDto view) {
        return htmlToPdf(renderHtml(view));
    }

    private QuoteViewDto viewOf(UUID ownerId, UUID quoteId) {
        Quote quote = quoteService.findById(ownerId, quoteId);
        return QuoteViewDto.from(quote);
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // O segundo argumento é a base para URLs relativas (imagens, CSS externo).
            // O template é autossuficiente, então não precisa de base.
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate quote PDF", e);
        }
    }
}
