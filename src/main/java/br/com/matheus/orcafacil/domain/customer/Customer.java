package br.com.matheus.orcafacil.domain.customer;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.quote.entity.Quote;
import br.com.matheus.orcafacil.domain.shared.Address;
import br.com.matheus.orcafacil.domain.shared.BaseEntity;
import br.com.matheus.orcafacil.domain.shared.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "customers",
        schema = "app",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customers_business_tax_id",
                columnNames = {"business_id", "tax_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 20)
    private PersonType personType;

    @Column(nullable = false)
    private String name;

    @Column(name = "tax_id", length = 14)
    private String taxId;

    private String email;

    private String phone;

    @Embedded
    private Address address;

    private String notes;

    @Setter(AccessLevel.NONE)
    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @OneToMany(mappedBy = "customer")
    @Setter(AccessLevel.NONE)
    private List<Quote> quotes = new ArrayList<>();

    public Customer(
            PersonType personType,
            String name,
            String taxId,
            String email,
            String phone,
            Address address,
            String notes
    ) {
        this.personType = personType;
        this.name = name;
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
    }

    public boolean isActive() {
        return deactivatedAt == null;
    }

    public void deactivate() {
        if (isActive()) {
            deactivatedAt = Instant.now();
        }
    }

    public void reactivate() {
        deactivatedAt = null;
    }
}
