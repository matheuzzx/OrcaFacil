package br.com.matheus.orcafacil.domain.quote.web.dto;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.customer.Customer;
import br.com.matheus.orcafacil.domain.quote.QuoteStatus;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.quote.entity.QuoteItem;
import br.com.matheus.orcafacil.domain.shared.PersonType;
import br.com.matheus.orcafacil.shared.format.BrazilianFormat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dados do orçamento já formatados para o documento (preview HTML e, depois, PDF).
 * O template só exibe strings: nenhuma regra ou formatação fica no HTML.
 */
public record QuoteViewDto(
        String number,
        String title,
        String status,
        String statusLabel,
        String issueDate,
        String validUntil,
        BusinessView business,
        CustomerView customer,
        List<ItemView> items,
        String subtotal,
        String discount,
        boolean hasDiscount,
        String total,
        String notes,
        String terms
) {

    public record BusinessView(
            String name,
            String legalName,
            String initials,
            String logoUrl,
            String taxIdLabel,
            String taxId,
            String email,
            String phone,
            String address
    ) {
    }

    public record CustomerView(
            String name,
            String typeLabel,
            String taxIdLabel,
            String taxId,
            String email,
            String phone,
            String address
    ) {
    }

    public record ItemView(
            String description,
            String quantity,
            String unitPrice,
            String total
    ) {
    }

    /** Precisa ser chamado dentro de uma transação: lê business, customer e items (LAZY). */
    public static QuoteViewDto from(Quote quote) {
        BigDecimal discount = quote.getDiscount() == null ? BigDecimal.ZERO : quote.getDiscount();

        return new QuoteViewDto(
                "%04d".formatted(quote.getNumber()),
                blankToNull(quote.getTitle()),
                quote.getStatus().name(),
                statusLabel(quote.getStatus()),
                BrazilianFormat.date(quote.getIssueDate()),
                BrazilianFormat.date(quote.getValidUntil()),
                business(quote.getBusiness()),
                customer(quote.getCustomer()),
                quote.getItems().stream().map(QuoteViewDto::item).toList(),
                BrazilianFormat.money(quote.getSubtotal()),
                BrazilianFormat.money(discount),
                discount.signum() > 0,
                BrazilianFormat.money(quote.getTotal()),
                blankToNull(quote.getNotes()),
                blankToNull(quote.getTerms())
        );
    }

    private static BusinessView business(Business business) {
        return new BusinessView(
                business.getDisplayName(),
                blankToNull(business.getLegalName()),
                initials(business.getDisplayName()),
                blankToNull(business.getLogoUrl()),
                taxIdLabel(business.getPersonType()),
                BrazilianFormat.taxId(business.getTaxId()),
                blankToNull(business.getBusinessEmail()),
                blankToNull(business.getPhone()),
                BrazilianFormat.address(business.getAddress())
        );
    }

    private static CustomerView customer(Customer customer) {
        return new CustomerView(
                customer.getName(),
                customer.getPersonType() == PersonType.COMPANY ? "Pessoa jurídica" : "Pessoa física",
                taxIdLabel(customer.getPersonType()),
                BrazilianFormat.taxId(customer.getTaxId()),
                blankToNull(customer.getEmail()),
                blankToNull(customer.getPhone()),
                BrazilianFormat.address(customer.getAddress())
        );
    }

    private static ItemView item(QuoteItem item) {
        String quantity = BrazilianFormat.quantity(item.getQuantity());
        if (item.getUnit() != null && !item.getUnit().isBlank()) {
            quantity += " " + item.getUnit().trim();
        }
        return new ItemView(
                item.getDescription(),
                quantity,
                BrazilianFormat.money(item.getUnitPrice()),
                BrazilianFormat.money(item.getTotal())
        );
    }

    private static String statusLabel(QuoteStatus status) {
        return switch (status) {
            case DRAFT -> "Rascunho";
            case SENT -> "Enviado";
            case APPROVED -> "Aprovado";
            case REJECTED -> "Recusado";
            case EXPIRED -> "Expirado";
            case CANCELLED -> "Cancelado";
        };
    }

    private static String taxIdLabel(PersonType personType) {
        return personType == PersonType.COMPANY ? "CNPJ" : "CPF";
    }

    /** "Silva Reformas Ltda" -> "SR"; usado quando não há logo. */
    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(word -> word.length() > 2)
                .limit(2)
                .map(word -> word.substring(0, 1).toUpperCase(BrazilianFormat.PT_BR))
                .collect(Collectors.joining())
                .transform(result -> result.isEmpty() ? name.trim().substring(0, 1).toUpperCase() : result);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
