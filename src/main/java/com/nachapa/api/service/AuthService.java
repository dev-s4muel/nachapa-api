package com.nachapa.api.service;


import com.nachapa.api.dto.login.LoginRequestDto;

public interface AuthService {

    String authenticate(LoginRequestDto loginRequest);
}