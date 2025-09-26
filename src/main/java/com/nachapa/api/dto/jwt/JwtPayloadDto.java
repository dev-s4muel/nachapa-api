package com.nachapa.api.dto.jwt;

import com.nachapa.api.auth.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPayloadDto {
    private String email;
    private RoleEnum role;
}