package br.com.matheus.orcafacil.domain.customer;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.business.BusinessService;
import br.com.matheus.orcafacil.shared.exception.ConflictException;
import br.com.matheus.orcafacil.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessService businessService;

    @Transactional(readOnly = true)
    public List<Customer> findAllActive(UUID ownerId) {
        return customerRepository
                .findAllByBusinessOwnerIdAndDeactivatedAtIsNullOrderByNameAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllDeactivated(UUID ownerId) {
        return customerRepository
                .findAllByBusinessOwnerIdAndDeactivatedAtIsNotNullOrderByNameAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID ownerId, UUID customerId) {
        return customerRepository.findByIdAndBusinessOwnerId(customerId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public Customer create(UUID ownerId, Customer customer) {
        Business business = businessService.findByOwnerId(ownerId);

        validateUniqueTaxId(business.getId(), customer.getTaxId(), null);

        customer.setBusiness(business);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(UUID ownerId, UUID customerId, Customer changes) {
        Customer customer = findActiveById(ownerId, customerId);

        if (changes.getTaxId() != null) {
            validateUniqueTaxId(customer.getBusiness().getId(), changes.getTaxId(), customerId);
        }

        if (changes.getPersonType() != null) {
            customer.setPersonType(changes.getPersonType());
        }
        if (changes.getName() != null) {
            customer.setName(changes.getName());
        }
        if (changes.getTaxId() != null) {
            customer.setTaxId(changes.getTaxId());
        }
        if (changes.getEmail() != null) {
            customer.setEmail(changes.getEmail());
        }
        if (changes.getPhone() != null) {
            customer.setPhone(changes.getPhone());
        }
        if (changes.getAddress() != null) {
            customer.setAddress(changes.getAddress());
        }
        if (changes.getNotes() != null) {
            customer.setNotes(changes.getNotes());
        }

        return customer;
    }

    @Transactional
    public Customer deactivate(UUID ownerId, UUID customerId) {
        Customer customer = findActiveById(ownerId, customerId);
        customer.deactivate();
        return customer;
    }

    @Transactional
    public Customer reactivate(UUID ownerId, UUID customerId) {
        Customer customer = findById(ownerId, customerId);
        customer.reactivate();
        return customer;
    }

    /**
     * Public because {@code QuoteService} (a different package) needs to resolve
     * an active customer when creating or reassigning a quote.
     */
    @Transactional(readOnly = true)
    public Customer findActiveById(UUID ownerId, UUID customerId) {
        return customerRepository
                .findByIdAndBusinessOwnerIdAndDeactivatedAtIsNull(customerId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active customer not found"));
    }

    private void validateUniqueTaxId(UUID businessId, String taxId, UUID customerId) {
        if (taxId == null) {
            return;
        }

        boolean taxIdExists = customerId == null
                ? customerRepository.existsByBusinessIdAndTaxId(businessId, taxId)
                : customerRepository.existsByBusinessIdAndTaxIdAndIdNot(businessId, taxId, customerId);

        if (taxIdExists) {
            throw new ConflictException("Customer tax ID already exists for this business");
        }
    }
}
