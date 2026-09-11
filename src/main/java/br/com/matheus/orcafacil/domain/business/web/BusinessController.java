package br.com.matheus.orcafacil.domain.business.web;

import br.com.matheus.orcafacil.domain.business.Business;
import br.com.matheus.orcafacil.domain.business.BusinessService;
import br.com.matheus.orcafacil.domain.business.web.dto.BusinessRequest;
import br.com.matheus.orcafacil.domain.business.web.dto.BusinessResponse;
import br.com.matheus.orcafacil.domain.business.web.dto.BusinessUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/me")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping
    public BusinessResponse findCurrentBusiness(@AuthenticationPrincipal Jwt jwt) {
        Business business = businessService.findByOwnerId(ownerId(jwt));
        return BusinessResponse.from(business);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BusinessRequest request
    ) {
        Business business = businessService.create(ownerId(jwt), toEntity(request));
        return BusinessResponse.from(business);
    }

    @PatchMapping
    public BusinessResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BusinessUpdateRequest request
    ) {
        Business business = businessService.update(ownerId(jwt), toEntity(request));
        return BusinessResponse.from(business);
    }

    private UUID ownerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private Business toEntity(BusinessRequest request) {
        return new Business(
                request.personType(),
                request.displayName(),
                request.legalName(),
                request.taxId(),
                request.businessEmail(),
                request.phone(),
                request.logoUrl(),
                request.address() == null ? null : request.address().toEntity()
        );
    }

    private Business toEntity(BusinessUpdateRequest request) {
        return new Business(
                request.personType(),
                request.displayName(),
                request.legalName(),
                request.taxId(),
                request.businessEmail(),
                request.phone(),
                request.logoUrl(),
                request.address() == null ? null : request.address().toEntity()
        );
    }
}
