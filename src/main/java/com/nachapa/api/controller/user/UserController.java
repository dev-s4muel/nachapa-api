package com.nachapa.api.controller.user;

import com.nachapa.api.dto.user.UserRequestDto;
import com.nachapa.api.dto.user.UserResponseDto;
import com.nachapa.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/users")
@Tag(name = "Users", description = "Operações de usuários (CRUD, listagem paginada)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Inativar usuário (DELETE)",
            description = "Inativa (soft delete) o usuário pelo ID. Retorna 204 em caso de sucesso."
    )
    @ApiResponse(responseCode = "204", description = "Usuário inativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deactivateUserById(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable UUID id) {

        log.info("Tentativa de inativar usuario com id {} no sistema.", id);
        userService.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualizar usuário (PUT)",
            description = "Atualiza completamente o cadastro do usuário pelo ID."
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateClient(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable("id") UUID userId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRequestDto.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de atualização",
                                    value = """
                                            {
                                              "nome": "Maria Souza",
                                              "email": "maria@email.com",
                                              "senha": "novaSenha123",
                                              "cpf": "20716166003",
                                              "telefone": "31998765432",
                                              "data-nascimento": "20/10/1998"
                                            }
                                            """
                            )
                    )
            )
            UserRequestDto userRequestDto) {
        UserResponseDto updated = userService.update(userId, userRequestDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Listar usuários (GET paginado)",
            description = "Retorna usuários com paginação e ordenação. Parâmetros: page, size, sort (ex.: name,asc)."
    )
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> listUsers(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenação no formato 'campo,direção' (ex.: name,asc)", example = "name,asc")
            @RequestParam(defaultValue = "name,asc") String sort,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = true) String token) {

        Page<UserResponseDto> users = userService.listUsers(page, size, sort);
        return ResponseEntity.ok(users);
    }
}