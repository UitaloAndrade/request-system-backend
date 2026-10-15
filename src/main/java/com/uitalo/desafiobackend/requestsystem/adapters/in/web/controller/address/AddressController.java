package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.address;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.AddressResponse;
import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.LookupAddressUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final LookupAddressUseCase lookupAddressUseCase;

    public AddressController(LookupAddressUseCase lookupAddressUseCase) {
        this.lookupAddressUseCase = lookupAddressUseCase;
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<AddressResponse> lookupByCep(
            @PathVariable String cep
    ) {
        PostalCodeLookup.PostalCodeAddress address =
                lookupAddressUseCase.lookup(cep);

        return ResponseEntity.ok(AddressResponse.from(address));
    }
}
