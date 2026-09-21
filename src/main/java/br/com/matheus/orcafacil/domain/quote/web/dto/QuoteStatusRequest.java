package br.com.matheus.orcafacil.domain.quote.web.dto;

import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import jakarta.validation.constraints.NotNull;

public record QuoteStatusRequest(
        @NotNull QuoteStatus status
) {
}
