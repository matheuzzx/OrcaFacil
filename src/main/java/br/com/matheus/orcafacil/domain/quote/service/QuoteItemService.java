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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Owns everything about a single quote item: finding it, validating it,
 * keeping its {@code total} in sync with quantity x unit price, and owning
 * {@code position} end to end — it is never accepted from a client request.
 * Positions are assigned on insertion (0-based, append order), kept
 * contiguous on removal, and only ever reassigned in bulk via
 * {@link #reorder(Quote, List)}. Quote-level concerns (totals across all
 * items, status, dates) belong to {@link QuoteService}.
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
        item.setPosition(quote.getItems().size());
        prepare(item);
        quote.addItem(item);
        return quoteItemRepository.save(item);
    }

    /**
     * Attaches a batch of items to a quote that has not been persisted yet,
     * assigning sequential positions in list order. Unlike {@link #add},
     * items are not saved individually — a not-yet-persisted quote has no
     * id for them to reference yet, so they ride along via
     * {@code CascadeType.ALL} when the quote itself is saved.
     */
    public void addAll(Quote quote, List<QuoteItem> items) {
        if (items == null) {
            return;
        }
        items.forEach(item -> {
            item.setPosition(quote.getItems().size());
            prepare(item);
            quote.addItem(item);
        });
    }

    @Transactional
    public QuoteItem update(
            UUID ownerId,
            UUID quoteId,
            UUID itemId,
            QuoteItem changes
    ) {
        QuoteItem item = findById(ownerId, quoteId, itemId);

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
        reindex(quote.getItems());
    }

    /**
     * Reassigns every item of the quote to the exact order given by
     * {@code itemIds} (0-based). {@code itemIds} must be a permutation of
     * the quote's current item ids — no more, no less, no duplicates.
     */
    @Transactional
    public void reorder(Quote quote, List<UUID> itemIds) {
        List<QuoteItem> items = quote.getItems();

        boolean isPermutationOfCurrentItems = itemIds.size() == items.size()
                && items.stream().map(QuoteItem::getId).collect(Collectors.toSet())
                        .equals(new HashSet<>(itemIds));

        if (!isPermutationOfCurrentItems) {
            throw new ConflictException(
                    "itemIds must contain exactly the quote's current items, each exactly once"
            );
        }

        Map<UUID, QuoteItem> itemsById = items.stream()
                .collect(Collectors.toMap(QuoteItem::getId, item -> item));

        for (int position = 0; position < itemIds.size(); position++) {
            itemsById.get(itemIds.get(position)).setPosition(position);
        }
    }

    /**
     * Re-validates and recalculates every item of a quote. Called by
     * {@link QuoteService} whenever the quote as a whole needs its totals
     * refreshed (create, update, add/update/remove item).
     */
    public void prepareAll(List<QuoteItem> items) {
        items.forEach(this::prepare);
    }

    private void reindex(List<QuoteItem> items) {
        for (int position = 0; position < items.size(); position++) {
            items.get(position).setPosition(position);
        }
    }

    private void prepare(QuoteItem item) {
        validate(item);
        calculateTotal(item);
    }

    private void validate(QuoteItem item) {
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
