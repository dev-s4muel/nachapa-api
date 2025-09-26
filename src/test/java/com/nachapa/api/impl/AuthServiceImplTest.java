package com.nachapa.api.impl;

import com.nachapa.api.auth.RoleEnum;
import com.nachapa.api.auth.User;
import com.nachapa.api.dto.jwt.JwtPayloadDto;
import com.nachapa.api.dto.login.LoginRequestDto;
import com.nachapa.api.exceptions.InvalidCredentialsException;
import com.nachapa.api.repository.UserRepository;
import com.nachapa.api.service.impl.AuthServiceImpl;
import com.nachapa.api.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("authenticate() deve retornar token quando email existir e senha for válida")
    void authenticate_success_returnsToken() {
        String email = "user@neoapp.com";
        String rawPassword = "secret";
        String encodedPassword = "$2a$10$hashFakeSoPraTeste";
        String expectedToken = "jwt-token";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        when(user.getPassword()).thenReturn(encodedPassword);
        when(user.getRole()).thenReturn(RoleEnum.USER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(any(JwtPayloadDto.class))).thenReturn(expectedToken);

        LoginRequestDto login = new LoginRequestDto(email, rawPassword);

        String token = authService.authenticate(login);

        assertThat(token).isEqualTo(expectedToken);

        ArgumentCaptor<JwtPayloadDto> payloadCaptor = ArgumentCaptor.forClass(JwtPayloadDto.class);
        verify(jwtUtil, times(1)).generateToken(payloadCaptor.capture());
        JwtPayloadDto payloadSent = payloadCaptor.getValue();
        assertThat(payloadSent.getEmail()).isEqualTo(email);
        assertThat(payloadSent.getRole()).isEqualTo(RoleEnum.USER);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    @DisplayName("authenticate() deve lançar InvalidCredentialsException quando usuário não existe")
    void authenticate_userNotFound_throwsInvalidCredentials() {
        String email = "dont.exists@neoapp.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        LoginRequestDto login = new LoginRequestDto(email, "any");

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(login));

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    @DisplayName("authenticate() deve lançar InvalidCredentialsException quando senha inválida")
    void authenticate_wrongPassword_throwsInvalidCredentials() {
        String email = "user@neoapp.com";
        String rawPassword = "wrong";
        String encodedPassword = "$2a$10$hashFakeSoPraTeste";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        when(user.getPassword()).thenReturn(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        LoginRequestDto login = new LoginRequestDto(email, rawPassword);

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(login));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil); // não deve chamar generateToken
    }
}