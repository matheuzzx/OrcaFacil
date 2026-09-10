package br.com.matheus.orcafacil.domain.quote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, UUID> {
}
