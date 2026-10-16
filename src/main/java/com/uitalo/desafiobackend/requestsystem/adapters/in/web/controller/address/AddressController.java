package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.address;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.AddressResponse;
import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.LookupAddressUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
@Tag(
        name = "Consulta de endereços",
        description = "Consulta e validação de endereços a partir do CEP."
)
public class AddressController {

    private final LookupAddressUseCase lookupAddressUseCase;

    public AddressController(LookupAddressUseCase lookupAddressUseCase) {
        this.lookupAddressUseCase = lookupAddressUseCase;
    }

    @Operation(
            summary = "Consultar endereço por CEP",
            description = """
        Consulta um CEP na integração com o ViaCEP e retorna
        logradouro, bairro, cidade e UF.
        Retorna erro quando o CEP é inválido, não é encontrado
        ou ocorre uma falha na consulta externa.
        """
    )
    @GetMapping("/cep/{cep}")
    public ResponseEntity<AddressResponse> lookupByCep(
            @PathVariable String cep
    ) {
        PostalCodeLookup.PostalCodeAddress address =
                lookupAddressUseCase.lookup(cep);

        return ResponseEntity.ok(AddressResponse.from(address));
    }
}
