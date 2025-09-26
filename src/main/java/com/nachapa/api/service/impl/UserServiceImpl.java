package com.nachapa.api.service.impl;

import com.nachapa.api.auth.User;
import com.nachapa.api.dto.user.UserRequestDto;
import com.nachapa.api.dto.user.UserResponseDto;
import com.nachapa.api.exceptions.CpfAlreadyRegisteredException;
import com.nachapa.api.exceptions.CpfCannotBeChangedException;
import com.nachapa.api.exceptions.EmailAlreadyRegisteredException;
import com.nachapa.api.exceptions.ErrorDeactivateUserException;
import com.nachapa.api.exceptions.UserNotFoundException;
import com.nachapa.api.repository.UserRepository;
import com.nachapa.api.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    public void registerUser(UserRequestDto userRequestDTO) {
        log.info("Iniciando o registro do usuário com e-mail: {}", userRequestDTO.getEmail());

        // Verifica se o e-mail já está cadastrado
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            log.warn("Tentativa de registro com e-mail já cadastrado: {}", userRequestDTO.getEmail());
            throw new EmailAlreadyRegisteredException();
        }

        // Verifica se o CPF já está cadastrado
        if (userRepository.findByCpf(userRequestDTO.getCpf()).isPresent()) {
            log.warn("Tentativa de registro com CPF já cadastrado: {}", userRequestDTO.getCpf());
            throw new CpfAlreadyRegisteredException();
        }

        User user = new User(
                userRequestDTO.getName(),
                userRequestDTO.getEmail(),
                passwordEncoder.encode(userRequestDTO.getPassword()), // Criptografa a senha
                userRequestDTO.getCpf(),
                userRequestDTO.getCellPhone(),
                userRequestDTO.getBirthDate()
        );

        userRepository.save(user);
        log.info("Usuário com e-mail {} registrado com sucesso!", user.getEmail());
    }

    @Override
    public void deactivateUserById(UUID id) {
        log.info("Iniciando a inativar usuário com ID {} no sistema.", id);

        User user = userRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> {
            log.warn("Tentativa de inativar usuário com ID {} que não existe.", id);
            return new UserNotFoundException();
        });

        try {
            user.setActive(false);
            log.info("Usuário com ID {} inativado com sucesso.", id);
            userRepository.save(user);

        } catch (Exception ex) {
            log.error("Erro ao inativar o usuário com ID {}. Detalhes: {}", id, ex.getMessage());
            throw new ErrorDeactivateUserException();
        }
    }

    @Transactional
    @Override
    public UserResponseDto update(UUID userId, UserRequestDto userRequestDto) {
        log.info("Atualizando usuário {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userRepository.existsByEmailAndIdNot(userRequestDto.getEmail(), userId)) {
            log.warn("E-mail já cadastrado: {}", userRequestDto.getEmail());
            throw new EmailAlreadyRegisteredException();
        }

        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }

        if (!Objects.equals(user.getCpf(), userRequestDto.getCpf())) {
            throw new CpfCannotBeChangedException();
        }
        user.setCellPhone(userRequestDto.getCellPhone());
        user.setBirthDate(userRequestDto.getBirthDate());

        log.info("Usuário {} atualizado com sucesso.", userId);
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> listUsers(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        return userRepository.findAll(pageable)
                .map(UserResponseDto::from);
    }
}
