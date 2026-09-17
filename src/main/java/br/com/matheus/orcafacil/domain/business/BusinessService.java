package br.com.matheus.orcafacil.domain.business;

import br.com.matheus.orcafacil.shared.exception.ConflictException;
import br.com.matheus.orcafacil.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public Business findByOwnerId(UUID ownerId) {
        return businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
    }

    @Transactional
    public Business create(UUID ownerId, Business business) {
        if (businessRepository.existsByOwnerId(ownerId)) {
            throw new ConflictException("Business already exists for this owner");
        }

        business.setOwnerId(ownerId);
        return businessRepository.save(business);
    }

    @Transactional
    public Business update(UUID ownerId, Business changes) {
        Business business = findByOwnerId(ownerId);

        if (changes.getPersonType() != null) {
            business.setPersonType(changes.getPersonType());
        }
        if (changes.getDisplayName() != null) {
            business.setDisplayName(changes.getDisplayName());
        }
        if (changes.getLegalName() != null) {
            business.setLegalName(changes.getLegalName());
        }
        if (changes.getTaxId() != null) {
            business.setTaxId(changes.getTaxId());
        }
        if (changes.getBusinessEmail() != null) {
            business.setBusinessEmail(changes.getBusinessEmail());
        }
        if (changes.getPhone() != null) {
            business.setPhone(changes.getPhone());
        }
        if (changes.getLogoUrl() != null) {
            business.setLogoUrl(changes.getLogoUrl());
        }
        if (changes.getAddress() != null) {
            business.setAddress(changes.getAddress());
        }

        return business;
    }
}
