package br.com.matheus.orcafacil.domain.customer.web.dto;

import br.com.matheus.orcafacil.domain.customer.Customer;
import br.com.matheus.orcafacil.domain.shared.PersonType;
import br.com.matheus.orcafacil.shared.web.dto.AddressData;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID businessId,
        PersonType personType,
        String name,
        String taxId,
        String email,
        String phone,
        AddressData address,
        String notes,
        boolean active,
        Instant deactivatedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getBusiness().getId(),
                customer.getPersonType(),
                customer.getName(),
                customer.getTaxId(),
                customer.getEmail(),
                customer.getPhone(),
                AddressData.from(customer.getAddress()),
                customer.getNotes(),
                customer.isActive(),
                customer.getDeactivatedAt(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
