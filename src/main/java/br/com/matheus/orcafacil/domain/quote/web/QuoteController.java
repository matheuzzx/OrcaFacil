package br.com.matheus.orcafacil.domain.quote.web;

import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;
import br.com.matheus.orcafacil.domain.quote.service.QuoteService;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemReorderRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteItemUpdateRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteResponse;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteStatusRequest;
import br.com.matheus.orcafacil.domain.quote.web.dto.QuoteUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    public List<QuoteResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) QuoteStatus status
    ) {
        return quoteService.findAll(ownerId(jwt), status)
                .stream()
                .map(QuoteResponse::from)
                .toList();
    }

    @GetMapping("/{quoteId}")
    public QuoteResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId
    ) {
        Quote quote = quoteService.findById(ownerId(jwt), quoteId);
        return QuoteResponse.from(quote);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody QuoteRequest request
    ) {
        List<QuoteItem> items = request.items() == null
                ? List.of()
                : request.items().stream().map(this::toEntity).toList();

        Quote quote = quoteService.create(ownerId(jwt), request.customerId(), toEntity(request), items);
        return QuoteResponse.from(quote);
    }

    @PatchMapping("/{quoteId}")
    public QuoteResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @Valid @RequestBody QuoteUpdateRequest request
    ) {
        Quote quote = quoteService.update(
                ownerId(jwt),
                quoteId,
                request.customerId(),
                toEntity(request)
        );
        return QuoteResponse.from(quote);
    }

    @DeleteMapping("/{quoteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId
    ) {
        quoteService.delete(ownerId(jwt), quoteId);
    }

    @PatchMapping("/{quoteId}/status")
    public QuoteResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @Valid @RequestBody QuoteStatusRequest request
    ) {
        Quote quote = quoteService.changeStatus(ownerId(jwt), quoteId, request.status());
        return QuoteResponse.from(quote);
    }

    @PostMapping("/{quoteId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse addItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @Valid @RequestBody QuoteItemRequest request
    ) {
        Quote quote = quoteService.addItem(ownerId(jwt), quoteId, toEntity(request));
        return QuoteResponse.from(quote);
    }

    @PatchMapping("/{quoteId}/items/reorder")
    public QuoteResponse reorderItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @Valid @RequestBody QuoteItemReorderRequest request
    ) {
        Quote quote = quoteService.reorderItems(ownerId(jwt), quoteId, request.itemIds());
        return QuoteResponse.from(quote);
    }

    @PatchMapping("/{quoteId}/items/{itemId}")
    public QuoteResponse updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @PathVariable UUID itemId,
            @Valid @RequestBody QuoteItemUpdateRequest request
    ) {
        Quote quote = quoteService.updateItem(ownerId(jwt), quoteId, itemId, toEntity(request));
        return QuoteResponse.from(quote);
    }

    @DeleteMapping("/{quoteId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID quoteId,
            @PathVariable UUID itemId
    ) {
        quoteService.removeItem(ownerId(jwt), quoteId, itemId);
    }

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private Quote toEntity(QuoteRequest request) {
        return new Quote(
                request.title(),
                request.issueDate(),
                request.validUntil(),
                request.discount(),
                request.notes(),
                request.terms()
        );
    }

    private Quote toEntity(QuoteUpdateRequest request) {
        return new Quote(
                request.title(),
                request.issueDate(),
                request.validUntil(),
                request.discount(),
                request.notes(),
                request.terms()
        );
    }

    private QuoteItem toEntity(QuoteItemRequest request) {
        return new QuoteItem(
                request.description(),
                request.quantity(),
                request.unit(),
                request.unitPrice()
        );
    }

    private QuoteItem toEntity(QuoteItemUpdateRequest request) {
        return new QuoteItem(
                request.description(),
                request.quantity(),
                request.unit(),
                request.unitPrice()
        );
    }
}
