package com.nachapa.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "User")
@Table(name = "tb_user")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres.")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "O CPF é obrigatório.")
    @Size(min = 11, max = 11, message = "CPF inválido. Use o formato 00000000000.")
    @Column(nullable = false)
    private String cpf;

    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern(regexp = "^[1-9]{2}9[0-9]{8}$", message = "O telefone deve estar no formato DDD + número (ex: 31912345678)")
    @Column(name = "cell_Phone")
    private String cellPhone;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser uma data no passado.")
    @Column(nullable = false, name = "birth_Date")
    private LocalDate birthDate;

    @Column(name = "is_Active", nullable = false)
    private boolean isActive;


    public User(String name, String email, String password, String cpf, String cellPhone, LocalDate birthDate) {
        this.role = RoleEnum.USER;
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.name = name;
        this.email = email;
        this.password = password;
        this.cpf = cpf;
        this.cellPhone = cellPhone;
        this.birthDate = birthDate;
    }

}