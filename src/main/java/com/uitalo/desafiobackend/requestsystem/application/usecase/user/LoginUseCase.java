package com.uitalo.desafiobackend.requestsystem.application.usecase.user;

import com.uitalo.desafiobackend.requestsystem.application.usecase.user.dto.LoginResult;

public interface LoginUseCase {

    LoginResult login(String email, String password);
}