package com.nachapa.api.controller.auth;

import com.nachapa.api.dto.jwt.JwtResponseDto;
import com.nachapa.api.dto.login.LoginRequestDto;
import com.nachapa.api.dto.user.UserRequestDto;
import com.nachapa.api.service.AuthService;
import com.nachapa.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/auth")
@Tag(name = "Auth", description = "Registro e autenticação (JWT)")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário. Retorna 201 em caso de sucesso."
    )
    @ApiResponse(responseCode = "201", description = "Criado")
    @ApiResponse(responseCode = "400", description = "Requisição inválida",
            content = @Content(mediaType = "application/json"))
    @PostMapping("/register")
    public ResponseEntity<Void> createNewAccount(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de registro",
                                    value = """
                                            {
                                              "nome": "Maria Silva",
                                              "email": "maria@email.com",
                                              "senha": "secret123",
                                              "cpf": "20716166003",
                                              "telefone": "31998765432",
                                              "data-nascimento": "20/10/1998"
                                            }
                                            """
                            )
                    )
            )
            UserRequestDto userRequestDto
    ) {
        log.info("Tentativa de registro para o email: {}", userRequestDto.getEmail());
        userService.registerUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Login",
            description = "Autentica o usuário e retorna um token JWT."
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponseDto.class),
                    examples = @ExampleObject(
                            name = "Resposta (token)",
                            value = """
                                    { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
            content = @Content(mediaType = "application/json"))
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de login",
                                    value = """
                                            {
                                              "email": "maria@email.com",
                                              "password": "secret123"
                                            }
                                            """
                            )
                    )
            )
            LoginRequestDto loginRequest
    ) {
        log.info("Tentativa de login para o email: {}", loginRequest.getEmail());
        String token = authService.authenticate(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new JwtResponseDto(token));
    }
}
