package br.com.matheus.orcafacil.domain.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "address_street")
    private String street;

    @Column(name = "address_number")
    private String number;

    @Column(name = "address_complement")
    private String complement;

    @Column(name = "address_district")
    private String district;

    @Column(name = "address_city")
    private String city;

    @Column(name = "address_state", length = 2)
    private String state;

    @Column(name = "address_postal_code", length = 9)
    private String postalCode;

    public Address(String street, String number, String complement, String district,
                   String city, String state, String postalCode) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.district = district;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
    }
}
