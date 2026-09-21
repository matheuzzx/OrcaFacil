package br.com.matheus.orcafacil.domain.customer.web;

import br.com.matheus.orcafacil.domain.customer.Customer;
import br.com.matheus.orcafacil.domain.customer.CustomerService;
import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerRequest;
import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerResponse;
import br.com.matheus.orcafacil.domain.customer.web.dto.CustomerUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public List<CustomerResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "false") boolean deactivated
    ) {
        List<Customer> customers = deactivated
                ? customerService.findAllDeactivated(ownerId(jwt))
                : customerService.findAllActive(ownerId(jwt));

        return customers.stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID customerId
    ) {
        Customer customer = customerService.findById(ownerId(jwt), customerId);
        return CustomerResponse.from(customer);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CustomerRequest request
    ) {
        Customer customer = customerService.create(ownerId(jwt), toEntity(request));
        return CustomerResponse.from(customer);
    }

    @PatchMapping("/{customerId}")
    public CustomerResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        Customer customer = customerService.update(ownerId(jwt), customerId, toEntity(request));
        return CustomerResponse.from(customer);
    }

    @PatchMapping("/{customerId}/deactivate")
    public CustomerResponse deactivate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID customerId
    ) {
        Customer customer = customerService.deactivate(ownerId(jwt), customerId);
        return CustomerResponse.from(customer);
    }

    @PatchMapping("/{customerId}/reactivate")
    public CustomerResponse reactivate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID customerId
    ) {
        Customer customer = customerService.reactivate(ownerId(jwt), customerId);
        return CustomerResponse.from(customer);
    }

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private Customer toEntity(CustomerRequest request) {
        return new Customer(
                request.personType(),
                request.name(),
                request.taxId(),
                request.email(),
                request.phone(),
                request.address() == null ? null : request.address().toEntity(),
                request.notes()
        );
    }

    private Customer toEntity(CustomerUpdateRequest request) {
        return new Customer(
                request.personType(),
                request.name(),
                request.taxId(),
                request.email(),
                request.phone(),
                request.address() == null ? null : request.address().toEntity(),
                request.notes()
        );
    }
}
