package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;

public interface LookupAddressUseCase {

    PostalCodeLookup.PostalCodeAddress lookup(String postalCode);
}