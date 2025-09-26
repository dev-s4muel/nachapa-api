package com.nachapa.api.util;

import com.nachapa.api.dto.jwt.JwtPayloadDto;
import com.nachapa.api.exceptions.JwtKeyMissingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private SecretKey secretKey;

    private JwtParser parser;

    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final String BEARER_PREFIX = "Bearer ";

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new JwtKeyMissingException();
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // tolerância de 5s para evitar flutuação de relógio em testes/ambiente
        this.parser = Jwts.parserBuilder().setSigningKey(secretKey).setAllowedClockSkewSeconds(5).build();
    }

    public String generateToken(JwtPayloadDto jwtPayloadDto) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationTime);
        return Jwts.builder().setSubject(jwtPayloadDto.getEmail()).claim("role", jwtPayloadDto.getRole()).setIssuedAt(now).setExpiration(exp).signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    //     Remove prefixo "Bearer " se presente.
    private String rawToken(String tokenOrBearer) {
        if (tokenOrBearer == null) return null;
        return tokenOrBearer.startsWith(BEARER_PREFIX) ? tokenOrBearer.substring(BEARER_PREFIX_LENGTH) : tokenOrBearer;
    }

    //     Extrai claims; pode lançar ExpiredJwtException/SignatureException se o chamador quiser tratar.
    public Claims extractClaims(String tokenOrBearer) {
        String token = rawToken(tokenOrBearer);
        return parser.parseClaimsJws(token).getBody();
    }

    //     Extrai o subject (email) a partir de um header Authorization: Bearer ...
    public String extractEmailUser(String bearer) {
        return extractClaims(bearer).getSubject();
    }

    //     Retorna true se expirado; não propaga ExpiredJwtException.
    public boolean isTokenExpired(String tokenOrBearer) {
        try {
            Date exp = extractClaims(tokenOrBearer).getExpiration();
            return exp.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            // token inválido por qualquer outro motivo (assinatura, formato, etc.)
            return true;
        }
    }

    //     Validação booleana: subject bate e não expirou; não propaga exceções.
    public boolean isTokenValid(String tokenOrBearer, String expectedEmail) {
        try {
            Claims claims = extractClaims(tokenOrBearer);
            String subject = claims.getSubject();
            return expectedEmail.equals(subject) && !isTokenExpired(tokenOrBearer);
        } catch (JwtException e) {
            return false;
        }
    }
}