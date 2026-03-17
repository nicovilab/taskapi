package com.taskapi.taskapi.security.jwt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-32-chars-minimum-ok!";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp(){
        provider = new JwtTokenProvider(SECRET, 3_600_000L);
    }

    @Test
    void generateToken_returnsNonEmptyString(){
        String token = provider.generateToken(authOf("alice", "ROLE_USER"));
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_containsThreeParts(){
        String token = provider.generateToken(authOf("alice", "ROLE_USER"));
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void isValid_withValidToken_returnsTrue(){
        String token = provider.generateToken(authOf("alice", "ROLE_USER"));
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    void isValid_withExpiredToken_returnFalse() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L);
        String token = shortLived.generateToken(authOf("alice", "ROLE_USER"));
        Thread.sleep(10);
        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void getUsername_extractCorrectSubject(){
        String token = provider.generateToken(authOf("alice", "ROLE_USER"));
        assertThat(provider.getUsername(token)).isEqualTo("alice");
    }

    @Test
    void getRoles_extractsCorrectRole(){
        String token = provider.generateToken(authOf("alice", "ROLE_USER"));
        assertThat(provider.getRoles(token)).containsExactly("ROLE_USER");
    }

    @Test
    void getRoles_doesNotIncludeNonRoleAuthorities(){
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN")));

        String token = provider.generateToken(auth);
        assertThat(provider.getRoles(token)).containsExactly("ROLE_ADMIN").doesNotContain("ADMIN");
    }

    //helper
    private Authentication authOf(String username, String role){
        return new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));
    }
}

