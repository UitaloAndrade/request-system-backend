package com.uitalo.desafiobackend.requestsystem.domain.model;

import java.util.Locale;

public record Address(
        String cep,
        String street,
        String city,
        String state,
        String number,
        String complement,
        boolean postalCodeValidated
) {
    public Address {
        cep = normalize(cep);
        street = normalize(street);
        city = normalize(city);
        state = state == null
                ? null
                : state.trim().toUpperCase(Locale.ROOT);
        number = normalize(number);
        complement = normalize(complement);

        if (cep != null
                && !cep.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "O CEP deve conter oito dígitos."
            );
        }

        if (state != null && !state.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException(
                    "A UF deve conter duas letras."
            );
        }

        if (number != null
                && (number.length() > 20 || number.isBlank())) {
            throw new IllegalArgumentException(
                    "O número deve ter entre 1 e 20 caracteres."
            );
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public boolean isComplete() {
        return cep != null
                && street != null
                && city != null
                && state != null
                && number != null
                && postalCodeValidated;
    }
}
