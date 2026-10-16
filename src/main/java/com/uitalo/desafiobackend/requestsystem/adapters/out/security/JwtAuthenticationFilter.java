package com.uitalo.desafiobackend.requestsystem.adapters.out.security;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception.ErrorResponseWriter;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            ErrorResponseWriter errorResponseWriter) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        try {
            UUID userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId).orElse(null);

            if (user == null || !user.isActive()) {
                SecurityContextHolder.clearContext();
                errorResponseWriter.write(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED",
                        "O usuário associado ao token não existe ou está inativo."
                );
                return;
            }

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    user.getId().toString(),
                    null,
                    authorities
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "O token de autenticação é inválido ou expirou."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}