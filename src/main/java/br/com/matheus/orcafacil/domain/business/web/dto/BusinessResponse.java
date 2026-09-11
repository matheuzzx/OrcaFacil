package br.com.matheus.orcafacil.domain.business.web.dto;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.shared.PersonType;

import java.time.Instant;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        UUID ownerId,
        PersonType personType,
        String displayName,
        String legalName,
        String taxId,
        String businessEmail,
        String phone,
        String logoUrl,
        AddressData address,
        Instant createdAt,
        Instant updatedAt
) {

    public static BusinessResponse from(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getOwnerId(),
                business.getPersonType(),
                business.getDisplayName(),
                business.getLegalName(),
                business.getTaxId(),
                business.getBusinessEmail(),
                business.getPhone(),
                business.getLogoUrl(),
                AddressData.from(business.getAddress()),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}
