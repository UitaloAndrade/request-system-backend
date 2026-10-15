package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.LookupAddressUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LookupAddressService implements LookupAddressUseCase {

    private final PostalCodeLookup postalCodeLookup;

    public LookupAddressService(PostalCodeLookup postalCodeLookup) {
        this.postalCodeLookup = postalCodeLookup;
    }

    @Override
    public PostalCodeLookup.PostalCodeAddress lookup(String postalCode) {

        if (postalCode == null || postalCode.isBlank()) {
            throw new BusinessRuleException("O CEP é obrigatório.");
        }

        String normalizedPostalCode = postalCode.replaceAll("\\D", "");

        if (!normalizedPostalCode.matches("\\d{8}")) {
            throw new BusinessRuleException(
                    "O CEP deve conter exatamente 8 dígitos."
            );
        }

        Optional<PostalCodeLookup.PostalCodeAddress> result =
                postalCodeLookup.findByPostalCode(normalizedPostalCode);

        return result.orElseThrow(() ->
                new BusinessRuleException(
                        "CEP não encontrado. Verifique o número informado."
                )
        );
    }
}
