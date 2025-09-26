package com.nachapa.api.util;


import com.nachapa.api.dto.jwt.JwtPayloadDto;
import com.nachapa.api.exceptions.JwtKeyMissingException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;

import static com.nachapa.api.auth.RoleEnum.ADMIN;
import static com.nachapa.api.auth.RoleEnum.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JwtUtil buildJwtUtil(String secret, long expirationMillis) {
        JwtUtil util = new JwtUtil();
        setField(util, "secret", secret);
        setField(util, "expirationTime", expirationMillis);
        util.init();
        return util;
    }

    @Test
    @DisplayName("init(): lança JwtKeyMissingException quando secret < 32 chars")
    void init_throwsWhenSecretTooShort() {
        JwtUtil util = new JwtUtil();
        setField(util, "secret", "short-secret");
        setField(util, "expirationTime", 3600000L);

        assertThatThrownBy(util::init).isInstanceOf(JwtKeyMissingException.class);
    }

    @Test
    @DisplayName("generateToken + extractClaims: subject e role corretos")
    void generate_and_extractClaims_success() {
        String secret = "0123456789ABCDEF0123456789ABCDEF"; // 32 chars
        JwtUtil util = buildJwtUtil(secret, Duration.ofHours(1).toMillis());

        String email = "user@neoapp.com";
        String role = "USER";
        String token = util.generateToken(new JwtPayloadDto(email, USER));

        assertThat(token).isNotBlank();

        Claims claims = util.extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
    }

    @Test
    @DisplayName("extractEmailUser(): aceita Bearer e devolve subject")
    void extractEmailUser_success() {
        String secret = "0123456789ABCDEF0123456789ABCDEF";
        JwtUtil util = buildJwtUtil(secret, Duration.ofMinutes(5).toMillis());

        String email = "maria@neoapp.com";
        String token = util.generateToken(new JwtPayloadDto(email, ADMIN));

        String bearer = "Bearer " + token;
        assertThat(util.extractEmailUser(bearer)).isEqualTo(email);
    }

    @Test
    @DisplayName("isTokenValid(): true quando subject bate e token não expirou")
    void isTokenValid_true() {
        String secret = "0123456789ABCDEF0123456789ABCDEF";
        JwtUtil util = buildJwtUtil(secret, Duration.ofMinutes(5).toMillis());

        String email = "valid@neoapp.com";
        String token = util.generateToken(new JwtPayloadDto(email, USER));

        assertThat(util.isTokenValid(token, email)).isTrue();
        assertThat(util.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid(): false quando email não bate")
    void isTokenValid_false_wrongEmail() {
        String secret = "0123456789ABCDEF0123456789ABCDEF";
        JwtUtil util = buildJwtUtil(secret, Duration.ofMinutes(5).toMillis());

        String token = util.generateToken(new JwtPayloadDto("owner@neoapp.com", USER));

        assertThat(util.isTokenValid(token, "other@neoapp.com")).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired(): true quando já nasce expirado (expTime negativo) e não propaga exception")
    void isTokenExpired_true_forPastExpiration() {
        String secret = "0123456789ABCDEF0123456789ABCDEF";
        JwtUtil util = buildJwtUtil(secret, -1L);

        String token = util.generateToken(new JwtPayloadDto("x@neoapp.com", USER));

        assertThat(util.isTokenExpired(token)).isTrue();           // não lança mais ExpiredJwtException
        assertThat(util.isTokenValid(token, "x@neoapp.com")).isFalse();
    }
}