package br.com.matheus.orcafacil.domain.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByBusinessOwnerIdAndDeactivatedAtIsNullOrderByNameAsc(UUID ownerId);

    List<Customer> findAllByBusinessOwnerIdAndDeactivatedAtIsNotNullOrderByNameAsc(UUID ownerId);

    Optional<Customer> findByIdAndBusinessOwnerId(UUID id, UUID ownerId);

    Optional<Customer> findByIdAndBusinessOwnerIdAndDeactivatedAtIsNull(UUID id, UUID ownerId);

    boolean existsByBusinessIdAndTaxId(UUID businessId, String taxId);

    boolean existsByBusinessIdAndTaxIdAndIdNot(UUID businessId, String taxId, UUID id);
}
