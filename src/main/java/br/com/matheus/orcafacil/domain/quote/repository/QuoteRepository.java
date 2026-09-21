package br.com.matheus.orcafacil.domain.quote.repository;

import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    List<Quote> findAllByBusinessOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<Quote> findAllByBusinessOwnerIdAndStatusOrderByCreatedAtDesc(
            UUID ownerId,
            QuoteStatus status
    );

    Optional<Quote> findByIdAndBusinessOwnerId(UUID id, UUID ownerId);

    @Query("select coalesce(max(q.number), 0) from Quote q where q.business.id = :businessId")
    Integer findHighestNumberByBusinessId(@Param("businessId") UUID businessId);
}
