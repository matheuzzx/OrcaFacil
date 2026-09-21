package br.com.matheus.orcafacil.domain.quote.service;

import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;
import br.com.matheus.orcafacil.domain.quote.repository.QuoteItemRepository;
import br.com.matheus.orcafacil.shared.exception.ConflictException;
import br.com.matheus.orcafacil.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Owns everything about a single quote item: finding it, validating it, and
 * keeping its {@code total} in sync with quantity x unit price. Quote-level
 * concerns (totals across all items, status, dates) belong to {@link QuoteService}.
 */
@Service
@RequiredArgsConstructor
public class QuoteItemService {

    private final QuoteItemRepository quoteItemRepository;

    @Transactional(readOnly = true)
    public QuoteItem findById(UUID ownerId, UUID quoteId, UUID itemId) {
        return quoteItemRepository
                .findByIdAndQuoteIdAndQuoteBusinessOwnerId(itemId, quoteId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote item not found"));
    }

    @Transactional
    public QuoteItem add(Quote quote, QuoteItem item) {
        prepare(item);
        quote.addItem(item);
        return quoteItemRepository.save(item);
    }

    @Transactional
    public QuoteItem update(
            UUID ownerId,
            UUID quoteId,
            UUID itemId,
            QuoteItem changes
    ) {
        QuoteItem item = findById(ownerId, quoteId, itemId);

        if (changes.getPosition() != null) {
            item.setPosition(changes.getPosition());
        }
        if (changes.getDescription() != null) {
            item.setDescription(changes.getDescription());
        }
        if (changes.getQuantity() != null) {
            item.setQuantity(changes.getQuantity());
        }
        if (changes.getUnit() != null) {
            item.setUnit(changes.getUnit());
        }
        if (changes.getUnitPrice() != null) {
            item.setUnitPrice(changes.getUnitPrice());
        }

        prepare(item);
        return item;
    }

    @Transactional
    public void remove(Quote quote, UUID ownerId, UUID quoteId, UUID itemId) {
        QuoteItem item = findById(ownerId, quoteId, itemId);
        quote.removeItem(item);
    }

    /**
     * Re-validates and recalculates every item of a quote. Called by
     * {@link QuoteService} whenever the quote as a whole needs its totals
     * refreshed (create, update, add/update/remove item).
     */
    public void prepareAll(List<QuoteItem> items) {
        items.forEach(this::prepare);
    }

    private void prepare(QuoteItem item) {
        validate(item);
        calculateTotal(item);
    }

    private void validate(QuoteItem item) {
        if (item.getPosition() == null || item.getPosition() < 0) {
            throw new ConflictException("Quote item position must be zero or greater");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ConflictException("Quote item description is required");
        }
        if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
            throw new ConflictException("Quote item quantity must be greater than zero");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
            throw new ConflictException("Quote item unit price cannot be negative");
        }
    }

    private void calculateTotal(QuoteItem item) {
        item.setTotal(item.getQuantity()
                .multiply(item.getUnitPrice())
                .setScale(2, RoundingMode.HALF_UP));
    }
}
