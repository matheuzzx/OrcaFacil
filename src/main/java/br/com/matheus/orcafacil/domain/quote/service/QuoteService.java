package br.com.matheus.orcafacil.domain.quote.service;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.business.BusinessService;
import br.com.matheus.orcafacil.domain.customer.Customer;
import br.com.matheus.orcafacil.domain.customer.CustomerService;
import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;
import br.com.matheus.orcafacil.domain.quote.repository.QuoteRepository;
import br.com.matheus.orcafacil.shared.exception.ConflictException;
import br.com.matheus.orcafacil.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the quote as a whole: linking it to a business and customer, assigning
 * its sequential number, enforcing the DRAFT/SENT/APPROVED/REJECTED/EXPIRED/
 * CANCELLED status machine, and keeping subtotal/discount/total in sync with
 * its items. Per-item validation and per-item total lives in
 * {@link QuoteItemService}; this class delegates to it and never touches an
 * item's fields directly.
 */
@Service
@RequiredArgsConstructor
public class QuoteService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final QuoteRepository quoteRepository;
    private final QuoteItemService quoteItemService;
    private final BusinessService businessService;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public List<Quote> findAll(UUID ownerId, QuoteStatus status) {
        if (status == null) {
            return quoteRepository.findAllByBusinessOwnerIdOrderByCreatedAtDesc(ownerId);
        }
        return quoteRepository.findAllByBusinessOwnerIdAndStatusOrderByCreatedAtDesc(ownerId, status);
    }

    @Transactional(readOnly = true)
    public Quote findById(UUID ownerId, UUID quoteId) {
        return quoteRepository.findByIdAndBusinessOwnerId(quoteId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
    }

    @Transactional
    public Quote create(UUID ownerId, UUID customerId, Quote quote) {
        Business business = businessService.findByOwnerId(ownerId);
        Customer customer = customerService.findActiveById(ownerId, customerId);

        quote.setBusiness(business);
        quote.setCustomer(customer);
        quote.setNumber(nextNumber(business.getId()));
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setIssueDate(quote.getIssueDate() == null ? LocalDate.now() : quote.getIssueDate());
        quote.setDiscount(moneyOrZero(quote.getDiscount()));

        validateDates(quote);
        quoteItemService.prepareAll(quote.getItems());
        recalculateTotals(quote);

        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(
            UUID ownerId,
            UUID quoteId,
            UUID customerId,
            Quote changes
    ) {
        Quote quote = findEditableById(ownerId, quoteId);

        if (customerId != null) {
            quote.setCustomer(customerService.findActiveById(ownerId, customerId));
        }
        if (changes.getTitle() != null) {
            quote.setTitle(changes.getTitle());
        }
        if (changes.getIssueDate() != null) {
            quote.setIssueDate(changes.getIssueDate());
        }
        if (changes.getValidUntil() != null) {
            quote.setValidUntil(changes.getValidUntil());
        }
        if (changes.getDiscount() != null) {
            quote.setDiscount(moneyOrZero(changes.getDiscount()));
        }
        if (changes.getNotes() != null) {
            quote.setNotes(changes.getNotes());
        }
        if (changes.getTerms() != null) {
            quote.setTerms(changes.getTerms());
        }

        validateDates(quote);
        recalculateTotals(quote);
        return quote;
    }

    @Transactional
    public Quote addItem(UUID ownerId, UUID quoteId, QuoteItem item) {
        Quote quote = findEditableById(ownerId, quoteId);

        quoteItemService.add(quote, item);
        recalculateTotals(quote);

        return quote;
    }

    @Transactional
    public Quote updateItem(
            UUID ownerId,
            UUID quoteId,
            UUID itemId,
            QuoteItem changes
    ) {
        Quote quote = findEditableById(ownerId, quoteId);
        quoteItemService.update(ownerId, quoteId, itemId, changes);
        recalculateTotals(quote);

        return quote;
    }

    @Transactional
    public Quote removeItem(UUID ownerId, UUID quoteId, UUID itemId) {
        Quote quote = findEditableById(ownerId, quoteId);

        quoteItemService.remove(quote, ownerId, quoteId, itemId);
        recalculateTotals(quote);

        return quote;
    }

    @Transactional
    public Quote changeStatus(UUID ownerId, UUID quoteId, QuoteStatus newStatus) {
        Quote quote = findById(ownerId, quoteId);

        if (newStatus == null) {
            throw new ConflictException("Quote status is required");
        }
        if (quote.getStatus() == newStatus) {
            return quote;
        }
        if (!allowedTransitions(quote.getStatus()).contains(newStatus)) {
            throw new ConflictException(
                    "Invalid quote status transition from "
                            + quote.getStatus()
                            + " to "
                            + newStatus
            );
        }
        if (newStatus == QuoteStatus.SENT && quote.getItems().isEmpty()) {
            throw new ConflictException("A quote must have at least one item before being sent");
        }

        quote.setStatus(newStatus);
        return quote;
    }

    private Quote findEditableById(UUID ownerId, UUID quoteId) {
        Quote quote = findById(ownerId, quoteId);

        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ConflictException("Only draft quotes can be edited");
        }

        return quote;
    }

    private Integer nextNumber(UUID businessId) {
        return quoteRepository.findHighestNumberByBusinessId(businessId) + 1;
    }

    private void validateDates(Quote quote) {
        if (quote.getIssueDate() == null) {
            throw new ConflictException("Quote issue date is required");
        }
        if (quote.getValidUntil() == null) {
            throw new ConflictException("Quote validity date is required");
        }
        if (quote.getValidUntil().isBefore(quote.getIssueDate())) {
            throw new ConflictException("Quote validity date cannot be before issue date");
        }
    }

    private void recalculateTotals(Quote quote) {
        quoteItemService.prepareAll(quote.getItems());

        BigDecimal subtotal = quote.getItems()
                .stream()
                .map(QuoteItem::getTotal)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = moneyOrZero(quote.getDiscount());

        if (discount.signum() < 0) {
            throw new ConflictException("Quote discount cannot be negative");
        }
        if (discount.compareTo(subtotal) > 0) {
            throw new ConflictException("Quote discount cannot be greater than subtotal");
        }

        quote.setSubtotal(subtotal);
        quote.setDiscount(discount);
        quote.setTotal(subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Set<QuoteStatus> allowedTransitions(QuoteStatus currentStatus) {
        return switch (currentStatus) {
            case DRAFT -> Set.of(QuoteStatus.SENT, QuoteStatus.CANCELLED);
            case SENT -> Set.of(
                    QuoteStatus.APPROVED,
                    QuoteStatus.REJECTED,
                    QuoteStatus.EXPIRED,
                    QuoteStatus.CANCELLED
            );
            case APPROVED, REJECTED, EXPIRED, CANCELLED -> Set.of();
        };
    }
}
