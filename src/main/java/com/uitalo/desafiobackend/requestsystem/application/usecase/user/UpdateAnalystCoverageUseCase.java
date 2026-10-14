package com.uitalo.desafiobackend.requestsystem.application.usecase.user;

import java.util.List;
import java.util.UUID;

public interface UpdateAnalystCoverageUseCase {

    void updateCoverage(UUID analystId, List<String> states);
}
