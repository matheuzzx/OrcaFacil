package br.com.matheus.orcafacil.domain.business;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByOwnerId(UUID ownerId);

    boolean existsByOwnerId(UUID ownerId);
}
