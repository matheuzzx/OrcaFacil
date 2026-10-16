package br.com.matheus.orcafacil.domain.quote.service;

import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto.BusinessView;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto.CustomerView;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteViewDto.ItemView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa só a renderização (template + PDF), sem Spring e sem banco.
 * O QuoteService não é usado nesses caminhos, então pode ser null.
 */
class QuoteDocumentServiceTest {

    private QuoteDocumentService service;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        service = new QuoteDocumentService(null, engine);
    }

    @Test
    void rendersHtmlWithQuoteData() {
        String html = service.renderHtml(fullQuote());

        assertThat(html)
                .contains("Silva Reformas")
                .contains("Mariana Costa")
                .contains("Instalação hidráulica")
                .contains("R$ 1.850,00")
                .contains("Nº 0042");
    }

    @Test
    void generatesAValidPdf() {
        byte[] pdf = service.renderPdf(fullQuote());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void generatesPdfWhenOptionalFieldsAreMissing() {
        QuoteViewDto minimal = new QuoteViewDto(
                "0001", null, "DRAFT", "Rascunho", "15/10/2026", "22/10/2026",
                new BusinessView("Silva Reformas", null, "SR", null, "CPF", null, null, null, null),
                new CustomerView("Mariana Costa", "Pessoa física", "CPF", null, null, null, null),
                List.of(),
                "R$ 0,00", "R$ 0,00", false, "R$ 0,00",
                null, null
        );

        byte[] pdf = service.renderPdf(minimal);

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    private QuoteViewDto fullQuote() {
        return new QuoteViewDto(
                "0042", "Reforma de cozinha", "SENT", "Enviado", "15/10/2026", "22/10/2026",
                new BusinessView(
                        "Silva Reformas", "Silva Reformas Ltda", "SR", null, "CNPJ", "12.345.678/0001-90",
                        "contato@silva.com.br", "(11) 3456-7890", "Rua das Obras, 100 - Centro, São Paulo/SP"
                ),
                new CustomerView(
                        "Mariana Costa", "Pessoa física", "CPF", "123.456.789-00",
                        "mariana@email.com", "(11) 98888-7777", "Rua das Flores, 45 - Jardim, São Paulo/SP"
                ),
                List.of(
                        new ItemView("Instalação de revestimento", "12 m²", "R$ 85,00", "R$ 1.020,00"),
                        new ItemView("Instalação hidráulica", "1", "R$ 830,00", "R$ 830,00")
                ),
                "R$ 1.850,00", "R$ 100,00", true, "R$ 1.750,00",
                "Materiais inclusos.", "50% na aprovação e 50% na conclusão."
        );
    }
}
