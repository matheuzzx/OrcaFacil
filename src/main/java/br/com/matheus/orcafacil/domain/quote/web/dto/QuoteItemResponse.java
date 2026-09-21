package br.com.matheus.orcafacil.domain.quote.web.dto;

import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteItemResponse(
        UUID id,
        Integer position,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt
) {

    public static QuoteItemResponse from(QuoteItem item) {
        return new QuoteItemResponse(
                item.getId(),
                item.getPosition(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getTotal(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
