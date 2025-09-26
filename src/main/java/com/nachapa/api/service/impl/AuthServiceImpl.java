package com.nachapa.api.service.impl;

import com.nachapa.api.auth.User;
import com.nachapa.api.dto.jwt.JwtPayloadDto;
import com.nachapa.api.dto.login.LoginRequestDto;
import com.nachapa.api.exceptions.InvalidCredentialsException;
import com.nachapa.api.repository.UserRepository;
import com.nachapa.api.service.AuthService;
import com.nachapa.api.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public String authenticate(LoginRequestDto loginRequest) {
        log.info("Iniciando autenticação para o e-mail: {}", loginRequest.getEmail());
        // Verifica se o usuário existe
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado para o e-mail: {}", loginRequest.getEmail());
                    return new InvalidCredentialsException();
                });
        log.info("Usuário encontrado no banco de dados: {}", user.getEmail());

        // Verifica a senha
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.error("Senha inválida para o e-mail: {}", loginRequest.getEmail());
            throw new InvalidCredentialsException();
        }
        log.info("Senha validada com sucesso para o e-mail: {}", loginRequest.getEmail());

        // Gera o token JWT incluindo o papel do usuário
        JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                user.getEmail(),
                user.getRole()
        );
        String token = jwtUtil.generateToken(jwtPayloadDto);
        log.info("Token JWT gerado com sucesso para o e-mail: {}", loginRequest.getEmail());

        return token;
    }
}
