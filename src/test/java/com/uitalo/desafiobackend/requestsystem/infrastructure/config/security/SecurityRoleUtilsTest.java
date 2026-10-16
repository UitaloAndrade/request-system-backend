package com.uitalo.desafiobackend.requestsystem.infrastructure.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRoleUtilsTest {

    @Test
    void shouldRecognizeAdministratorRole() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin-id", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityRoleUtils.isAdmin(authentication)).isTrue();
    }

    @Test
    void shouldNotRecognizeClientOrAnalystAsAdministrator() {
        Authentication client = new UsernamePasswordAuthenticationToken(
                "client-id", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        Authentication analyst = new UsernamePasswordAuthenticationToken(
                "analyst-id", null, List.of(new SimpleGrantedAuthority("ROLE_ANALYST")));

        assertThat(SecurityRoleUtils.isAdmin(client)).isFalse();
        assertThat(SecurityRoleUtils.isAdmin(analyst)).isFalse();
        assertThat(SecurityRoleUtils.isAdmin(null)).isFalse();
    }
}
