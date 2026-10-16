package com.uitalo.desafiobackend.requestsystem.infrastructure.config.security;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception.ErrorResponseWriter;
import com.uitalo.desafiobackend.requestsystem.adapters.out.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ErrorResponseWriter errorResponseWriter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticação e documentação
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Administração
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Operações de análise
                        .requestMatchers("/analyst/**").hasRole("ANALYST")

                        // Consulta de CEP: qualquer usuário autenticado
                        .requestMatchers("/addresses/**").authenticated()

                        // Solicitações: somente clientes
                        .requestMatchers("/solicitations/**").hasRole("CLIENT")

                        // Demais endpoints exigem autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                errorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "UNAUTHORIZED",
                                        "É necessário autenticar-se para acessar este recurso."
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                errorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "ACCESS_DENIED",
                                        "Você não tem permissão para acessar este recurso."
                                ))
                );

        return http.build();
    }
}
