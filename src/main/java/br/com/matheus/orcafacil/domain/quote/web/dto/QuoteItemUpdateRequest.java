package br.com.matheus.orcafacil.domain.quote.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record QuoteItemUpdateRequest(
        @Size(min = 1, max = 255) String description,
        @Positive BigDecimal quantity,
        @Size(max = 20) String unit,
        @PositiveOrZero BigDecimal unitPrice
) {
}
