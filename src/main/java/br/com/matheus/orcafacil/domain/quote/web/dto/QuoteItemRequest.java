package br.com.matheus.orcafacil.domain.quote.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record QuoteItemRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull @Positive BigDecimal quantity,
        @Size(max = 20) String unit,
        @NotNull @PositiveOrZero BigDecimal unitPrice
) {
}
