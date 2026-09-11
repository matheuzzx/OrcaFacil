package br.com.matheus.orcafacil.domain.business.web.dto;

import br.com.matheus.orcafacil.domain.shared.PersonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BusinessRequest(
        @NotNull PersonType personType,
        @NotBlank @Size(max = 150) String displayName,
        @Size(max = 150) String legalName,
        @NotBlank
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "taxId must contain 11 or 14 digits")
        String taxId,
        @Email @Size(max = 254) String businessEmail,
        @Pattern(regexp = "\\d{10,11}", message = "phone must contain 10 or 11 digits")
        String phone,
        @Size(max = 2048) String logoUrl,
        @Valid AddressData address
) {
}
