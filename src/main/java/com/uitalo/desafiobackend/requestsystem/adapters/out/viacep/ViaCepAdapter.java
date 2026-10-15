package com.uitalo.desafiobackend.requestsystem.adapters.out.viacep;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.domain.execption.PostalCodeServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class ViaCepAdapter implements PostalCodeLookup {

    private final RestClient restClient;

    public ViaCepAdapter(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://viacep.com.br")
                .build();
    }

    @Override
    public Optional<PostalCodeAddress> findByPostalCode(String postalCode) {

        try {
            ViaCepResponse response = restClient.get()
                    .uri("/ws/{cep}/json/", postalCode)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null) {
                throw new PostalCodeServiceException(
                        "O ViaCEP retornou uma resposta vazia."
                );
            }

            if (Boolean.TRUE.equals(response.erro())) {
                return Optional.empty();
            }

            return Optional.of(new PostalCodeAddress(
                    response.cep(),
                    response.logradouro(),
                    response.bairro(),
                    response.cidade(),
                    response.uf()
            ));

        } catch (PostalCodeServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new PostalCodeServiceException(
                    "Não foi possível consultar o CEP no momento.",
                    exception
            );
        }
    }
}
