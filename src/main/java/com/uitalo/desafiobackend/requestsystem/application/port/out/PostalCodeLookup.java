package com.uitalo.desafiobackend.requestsystem.application.port.out;

import java.util.Optional;

public interface PostalCodeLookup {

    Optional<PostalCodeAddress> findByPostalCode(String postalCode);

    record PostalCodeAddress(
            String postalCode,
            String street,
            String neighborhood,
            String city,
            String state
    ) {}
}