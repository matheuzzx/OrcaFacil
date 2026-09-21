package br.com.matheus.orcafacil.domain.quote.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuoteRequest(
        @NotNull UUID customerId,
        @Size(max = 150) String title,
        LocalDate issueDate,
        @NotNull LocalDate validUntil,
        @PositiveOrZero BigDecimal discount,
        @Size(max = 2000) String notes,
        @Size(max = 2000) String terms,
        @Valid List<QuoteItemRequest> items
) {
}
