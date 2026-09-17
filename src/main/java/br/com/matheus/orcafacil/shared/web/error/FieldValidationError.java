package br.com.matheus.orcafacil.shared.web.error;

public record FieldValidationError(
        String field,
        String message
) {
}
