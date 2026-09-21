package br.com.matheus.orcafacil.domain.quote.repository;

import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, UUID> {

    Optional<QuoteItem> findByIdAndQuoteIdAndQuoteBusinessOwnerId(
            UUID id,
            UUID quoteId,
            UUID ownerId
    );
}
