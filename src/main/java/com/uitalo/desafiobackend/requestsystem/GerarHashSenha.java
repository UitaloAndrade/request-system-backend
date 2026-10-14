package com.uitalo.desafiobackend.requestsystem;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHashSenha {

    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();

        System.out.println(
                encoder.encode("adm12345")
        );
    }
}