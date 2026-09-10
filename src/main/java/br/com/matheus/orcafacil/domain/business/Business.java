package br.com.matheus.orcafacil.domain.business;

import br.com.matheus.orcafacil.domain.customer.Customer;
import br.com.matheus.orcafacil.domain.quote.Quote;
import br.com.matheus.orcafacil.domain.shared.Address;
import br.com.matheus.orcafacil.domain.shared.BaseEntity;
import br.com.matheus.orcafacil.domain.shared.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "businesses",
        schema = "app",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_businesses_owner", columnNames = "owner_id"),
                @UniqueConstraint(name = "uk_businesses_tax_id", columnNames = "tax_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Business extends BaseEntity {

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 20)
    private PersonType personType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "tax_id", nullable = false, length = 14)
    private String taxId;

    @Column(name = "business_email")
    private String businessEmail;

    private String phone;

    @Column(name = "logo_url")
    private String logoUrl;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "business")
    @Setter(AccessLevel.NONE)
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "business")
    @Setter(AccessLevel.NONE)
    private List<Quote> quotes = new ArrayList<>();
}
