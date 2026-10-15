package br.com.matheus.orcafacil.shared.format;

import br.com.matheus.orcafacil.domain.shared.Address;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Formatação no padrão brasileiro para documentos (preview HTML e PDF).
 * Fica fora do template para que o HTML só exiba strings prontas.
 */
public final class BrazilianFormat {

    public static final Locale PT_BR = Locale.of("pt", "BR");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private BrazilianFormat() {
    }

    public static String money(BigDecimal value) {
        // NumberFormat não é thread-safe: cria um por chamada.
        NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);
        return currency.format(value == null ? BigDecimal.ZERO : value)
                .replace(' ', ' ');
    }

    public static String quantity(BigDecimal value) {
        if (value == null) {
            return "";
        }
        DecimalFormat format = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(PT_BR));
        return format.format(value);
    }

    public static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    /** 11 dígitos vira CPF, 14 vira CNPJ; qualquer outra coisa é devolvida como veio. */
    public static String taxId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (digits.length() == 14) {
            return digits.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return raw;
    }

    /** "Rua X, 123 - Apto 4 - Centro, São Paulo/SP - CEP 01000-000" (pula partes vazias). */
    public static String address(Address address) {
        if (address == null) {
            return null;
        }
        String street = join(", ", address.getStreet(), address.getNumber());
        String cityState = join("/", address.getCity(), address.getState());
        String postalCode = isBlank(address.getPostalCode()) ? null : "CEP " + address.getPostalCode();

        String line = join(" - ", street, address.getComplement(), address.getDistrict());
        String result = join(" - ", join(", ", line, cityState), postalCode);
        return result.isEmpty() ? null : result;
    }

    private static String join(String separator, String... parts) {
        return Stream.of(parts)
                .filter(part -> !isBlank(part))
                .map(String::trim)
                .collect(Collectors.joining(separator));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
