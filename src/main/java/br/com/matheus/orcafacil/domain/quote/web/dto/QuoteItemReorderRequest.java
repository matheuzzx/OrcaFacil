package br.com.matheus.orcafacil.domain.quote.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Desired final order of a quote's items: {@code itemIds} must be a
 * permutation of the quote's current item ids — index 0 becomes position 0,
 * and so on.
 */
public record QuoteItemReorderRequest(
        @NotEmpty List<UUID> itemIds
) {
}
