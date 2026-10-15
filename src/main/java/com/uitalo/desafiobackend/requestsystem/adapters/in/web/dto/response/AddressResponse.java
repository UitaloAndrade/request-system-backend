package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;

public record AddressResponse(
        String postalCode,
        String street,
        String neighborhood,
        String city,
        String state
) {
    public static AddressResponse from(
            PostalCodeLookup.PostalCodeAddress address
    ) {
        return new AddressResponse(
                address.postalCode(),
                address.street(),
                address.neighborhood(),
                address.city(),
                address.state()
        );
    }
}