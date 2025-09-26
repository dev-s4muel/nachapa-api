package com.nachapa.api.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Setter
@Getter
public class UserRequestDto {

    @NotBlank(message = "O nome é obrigatório.")
    @JsonProperty("nome")
    private String name;

    @NotBlank(message = "O email é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @JsonProperty("senha")
    private String password;

    @NotBlank(message = "O cpf é obrigatório.")
    @CPF(message = "CPF Invalido. Formatos aceitos: 000.000.000-00 ou 00000000000")
    @JsonProperty("cpf")
    private String cpf;

    @Pattern(regexp = "^[1-9]{2}9[0-9]{8}$", message = "O telefone deve estar no formato DDD + número (ex: 31912345678)")
    @JsonProperty("telefone")
    private String cellPhone;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve estar no passado.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    // Define o formato aceito para entrada/saída JSON
    @JsonProperty("data-nascimento")
    private LocalDate birthDate;
}