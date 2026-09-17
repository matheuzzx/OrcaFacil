package br.com.matheus.orcafacil.domain.customer;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    @Test
    void shouldDeactivateAndReactivateCustomer() {
        Customer customer = new Customer();

        assertThat(customer.isActive()).isTrue();

        customer.deactivate();

        assertThat(customer.isActive()).isFalse();
        assertThat(customer.getDeactivatedAt()).isNotNull();

        customer.reactivate();

        assertThat(customer.isActive()).isTrue();
        assertThat(customer.getDeactivatedAt()).isNull();
    }

    @Test
    void shouldPreserveOriginalDeactivationTimestampWhenAlreadyDeactivated() {
        Customer customer = new Customer();

        customer.deactivate();
        Instant firstDeactivation = customer.getDeactivatedAt();
        customer.deactivate();

        assertThat(customer.getDeactivatedAt()).isEqualTo(firstDeactivation);
    }
}
