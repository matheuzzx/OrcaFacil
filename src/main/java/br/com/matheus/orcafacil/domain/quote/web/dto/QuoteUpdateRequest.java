package br.com.matheus.orcafacil.domain.quote.web.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record QuoteUpdateRequest(
        UUID customerId,
        @Size(max = 150) String title,
        LocalDate issueDate,
        LocalDate validUntil,
        @PositiveOrZero BigDecimal discount,
        @Size(max = 2000) String notes,
        @Size(max = 2000) String terms
) {
}
