package br.com.matheus.orcafacil.domain.business.web.dto;

import br.com.matheus.orcafacil.domain.shared.Address;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressData(
        @Size(max = 150) String street,
        @Size(max = 20) String number,
        @Size(max = 100) String complement,
        @Size(max = 100) String district,
        @Size(max = 100) String city,
        @Pattern(regexp = "[A-Z]{2}", message = "state must contain two uppercase letters")
        String state,
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "postalCode must be a valid Brazilian postal code")
        String postalCode
) {

    public Address toEntity() {
        return new Address(street, number, complement, district, city, state, postalCode);
    }

    public static AddressData from(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressData(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getDistrict(),
                address.getCity(),
                address.getState(),
                address.getPostalCode()
        );
    }
}
