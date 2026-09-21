package br.com.matheus.orcafacil.domain.quote.web.dto;

import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuoteResponse(
        UUID id,
        UUID businessId,
        UUID customerId,
        Integer number,
        String title,
        QuoteStatus status,
        LocalDate issueDate,
        LocalDate validUntil,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String notes,
        String terms,
        List<QuoteItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {

    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getBusiness().getId(),
                quote.getCustomer().getId(),
                quote.getNumber(),
                quote.getTitle(),
                quote.getStatus(),
                quote.getIssueDate(),
                quote.getValidUntil(),
                quote.getSubtotal(),
                quote.getDiscount(),
                quote.getTotal(),
                quote.getNotes(),
                quote.getTerms(),
                quote.getItems().stream().map(QuoteItemResponse::from).toList(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}
