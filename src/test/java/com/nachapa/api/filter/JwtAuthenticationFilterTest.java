package com.nachapa.api.filter;

import com.nachapa.api.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain chain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar quando Authorization = 'Bearer <token>' e role presente")
    void shouldAuthenticateWithValidBearerTokenAndRole() throws Exception {
        // request/response
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        String token = "abc.def.ghi";
        req.addHeader("Authorization", "Bearer " + token);

        // claims simuladas
        Claims claims = Jwts.claims().setSubject("user@neoapp.com");
        claims.put("role", "ADMIN");

        when(jwtUtil.extractClaims(token)).thenReturn(claims);

        // act
        filter.doFilterInternal(req, res, chain);

        // assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo("user@neoapp.com");
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");

        verify(jwtUtil).extractClaims(token);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Não deve autenticar quando não houver Authorization header")
    void shouldNotAuthenticateWithoutAuthorizationHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Não deve autenticar quando Authorization não iniciar com 'Bearer '")
    void shouldNotAuthenticateWithInvalidPrefix() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Token abc.def.ghi");

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Não deve autenticar quando role estiver ausente/vazia nas claims")
    void shouldNotAuthenticateWhenRoleMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        String token = "abc.def.ghi";
        req.addHeader("Authorization", "Bearer " + token);

        Claims claims = Jwts.claims().setSubject("user@neoapp.com");
        when(jwtUtil.extractClaims(token)).thenReturn(claims);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil).extractClaims(token);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Não deve sobrescrever autenticação existente no SecurityContext")
    void shouldNotOverrideExistingAuthentication() throws Exception {
        // Coloca uma auth pré-existente
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("preexistente@neoapp.com", null)
        );

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        String token = "abc.def.ghi";
        req.addHeader("Authorization", "Bearer " + token);

        Claims claims = Jwts.claims().setSubject("user@neoapp.com");
        claims.put("role", "USER");
        when(jwtUtil.extractClaims(token)).thenReturn(claims);

        filter.doFilterInternal(req, res, chain);

        // mantém a auth anterior (o filtro só seta auth se CONTEXTO estiver vazio)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getName()).isEqualTo("preexistente@neoapp.com");

        verify(jwtUtil).extractClaims(token); // ainda é chamado antes do if
        verify(chain).doFilter(req, res);
    }
}
