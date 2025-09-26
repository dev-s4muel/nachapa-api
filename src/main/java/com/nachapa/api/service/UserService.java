package com.nachapa.api.service;

import com.nachapa.api.dto.user.UserRequestDto;
import com.nachapa.api.dto.user.UserResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    void registerUser(UserRequestDto userRequestDto);

    void deactivateUserById(UUID id);

    UserResponseDto update(UUID userId, UserRequestDto userRequestDto);

    Page<UserResponseDto> listUsers(int page, int size, String sort);
}