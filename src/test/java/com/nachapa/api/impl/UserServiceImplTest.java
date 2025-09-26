package com.nachapa.api.impl;

import com.nachapa.api.auth.User;
import com.nachapa.api.dto.user.UserRequestDto;
import com.nachapa.api.dto.user.UserResponseDto;
import com.nachapa.api.exceptions.CpfAlreadyRegisteredException;
import com.nachapa.api.exceptions.CpfCannotBeChangedException;
import com.nachapa.api.exceptions.EmailAlreadyRegisteredException;
import com.nachapa.api.exceptions.ErrorDeactivateUserException;
import com.nachapa.api.exceptions.UserNotFoundException;
import com.nachapa.api.repository.UserRepository;
import com.nachapa.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private UserRequestDto request;

    private User existing;

    @BeforeEach
    void setUp() {
        request = new UserRequestDto();
        request.setName("Maria");
        request.setEmail("maria@neoapp.com");
        request.setPassword("secret123");
        request.setCpf("20716166003");
        request.setCellPhone("31999998888");
        request.setBirthDate(LocalDate.of(1998, 10, 20));

        existing = new User(
                "Usuario Atual",
                "atual@neoapp.com",
                "$2a$10$oldHash",
                "20716166003",       // mesmo CPF do request (para cenários sem troca)
                "31988887777",
                LocalDate.of(1995, 3, 15)
        );
    }

    @Test
    @DisplayName("registerUser: deve salvar quando email e cpf não existem")
    void registerUser_success() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.getCpf())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$newHash");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        service.registerUser(request);

        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Maria");
        assertThat(saved.getEmail()).isEqualTo("maria@neoapp.com");
        assertThat(saved.getCpf()).isEqualTo("20716166003");
        assertThat(saved.getCellPhone()).isEqualTo("31999998888");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1998, 10, 20));
        assertThat(saved.getPassword()).isEqualTo("$2a$10$newHash");
    }

    @Test
    @DisplayName("registerUser: deve lançar EmailAlreadyRegisteredException quando email já existe")
    void registerUser_emailDuplicado() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existing));

        assertThrows(EmailAlreadyRegisteredException.class, () -> service.registerUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: deve lançar CpfAlreadyRegisteredException quando cpf já existe")
    void registerUser_cpfDuplicado() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.getCpf())).thenReturn(Optional.of(existing));

        assertThrows(CpfAlreadyRegisteredException.class, () -> service.registerUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateUserById: deve inativar quando usuário ativo existe")
    void deactivateUser_success() {
        UUID id = UUID.randomUUID();
        User activeUser = new User(
                "Ativo",
                "ativo@neoapp.com",
                "$2a$10$hash",
                "20716166003",
                "31999990000",
                LocalDate.of(2000, 1, 1)
        );
        activeUser.setActive(true);

        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(activeUser));

        service.deactivateUserById(id);

        assertThat(activeUser.isActive()).isFalse();
        verify(userRepository).save(activeUser);
    }

    @Test
    @DisplayName("deactivateUserById: deve lançar UserNotFoundException quando não encontrar ativo")
    void deactivateUser_userNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.deactivateUserById(id));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateUserById: deve lançar ErrorDeactivateUserException se save falhar")
    void deactivateUser_saveError() {
        UUID id = UUID.randomUUID();
        User activeUser = new User(
                "Ativo",
                "ativo@neoapp.com",
                "$2a$10$hash",
                "20716166003",
                "31999990000",
                LocalDate.of(2000, 1, 1)
        );
        activeUser.setActive(true);

        when(userRepository.findByIdAndIsActiveTrue(id)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(ErrorDeactivateUserException.class, () -> service.deactivateUserById(id));
    }

    @Test
    @DisplayName("update: deve atualizar dados mantendo a senha quando password nulo ou blank")
    void update_success_passwordNotProvided() {
        UUID id = UUID.randomUUID();
        // usuário existente no banco
        existing.setEmail("atual@neoapp.com");
        existing.setPassword("$2a$10$oldHash");
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("maria@neoapp.com", id)).thenReturn(false);

        // request sem senha
        request.setPassword(null);
        request.setCpf("20716166003"); // igual ao existente → não lança CpfCannotBeChanged

        UserResponseDto response = service.update(id, request);

        // senha mantida
        assertThat(existing.getPassword()).isEqualTo("$2a$10$oldHash");
        // campos atualizados
        assertThat(response.getName()).isEqualTo("Maria");
        assertThat(response.getEmail()).isEqualTo("maria@neoapp.com");
        assertThat(response.getCellPhone()).isEqualTo("31999998888");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("update: deve atualizar e recriptografar senha quando informada")
    void update_success_passwordProvided() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("maria@neoapp.com", id)).thenReturn(false);

        request.setPassword("novaSenha123");
        request.setCpf("20716166003");

        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$newHash");

        UserResponseDto response = service.update(id, request);

        assertThat(existing.getPassword()).isEqualTo("$2a$10$newHash");
        assertThat(response.getEmail()).isEqualTo("maria@neoapp.com");
        verify(passwordEncoder).encode("novaSenha123");
    }

    @Test
    @DisplayName("update: deve lançar UserNotFoundException quando id não existir")
    void update_userNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.update(id, request));
    }

    @Test
    @DisplayName("update: deve lançar EmailAlreadyRegisteredException quando email pertence a outro id")
    void update_emailDuplicadoOutroUsuario() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> service.update(id, request));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("update: deve lançar CpfCannotBeChangedException quando CPF for alterado")
    void update_cpfAlterado() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(false);

        // muda cpf no request → deve falhar
        request.setCpf("01018023070");

        assertThrows(CpfCannotBeChangedException.class, () -> service.update(id, request));
    }

    @Test
    @DisplayName("listUsers: deve retornar página mapeada para UserResponseDto")
    void listUsers_success() {
        int page = 0, size = 2;
        String sort = "name,asc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        User u1 = new User(
                "A",
                "a@neoapp.com",
                "$",
                "12345678901",
                "31911112222",
                LocalDate.of(2000, 1, 1));

        User u2 = new User(
                "B",
                "b@neoapp.com",
                "$",
                "10987654321",
                "31922223333",
                LocalDate.of(1999, 2, 2));

        Page<User> users = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        Page<UserResponseDto> result = service.listUsers(page, size, sort);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("A");
        assertThat(result.getContent().get(1).getEmail()).isEqualTo("b@neoapp.com");
        verify(userRepository).findAll(any(Pageable.class));
    }
}