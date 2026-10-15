package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.LookupAddressUseCase;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.UpdateSolicitationAddressUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateSolicitationAddressService
        implements UpdateSolicitationAddressUseCase {

    private final SolicitationRepository solicitationRepository;
    private final LookupAddressUseCase lookupAddressUseCase;

    public UpdateSolicitationAddressService(
            SolicitationRepository solicitationRepository,
            LookupAddressUseCase lookupAddressUseCase) {
        this.solicitationRepository = solicitationRepository;
        this.lookupAddressUseCase = lookupAddressUseCase;
    }

    @Override
    @Transactional
    public Solicitation update(
            UUID solicitationId,
            UUID clientId,
            String cep,
            String number,
            String complement) {

        Solicitation solicitation = solicitationRepository
                .findByIdAndClientId(solicitationId, clientId)
                .orElseThrow(() -> new SolicitationNotFoundException(
                        "Solicitação não encontrada para o cliente informado."
                ));

        PostalCodeLookup.PostalCodeAddress postalAddress =
                lookupAddressUseCase.lookup(cep);

        Address address = new Address(
                postalAddress.postalCode(),
                postalAddress.street(),
                postalAddress.city(),
                postalAddress.neighborhood(),
                postalAddress.state(),
                number,
                complement,
                true
        );

        solicitation.updateAddress(address, Instant.now());

        return solicitationRepository.save(solicitation);
    }
}