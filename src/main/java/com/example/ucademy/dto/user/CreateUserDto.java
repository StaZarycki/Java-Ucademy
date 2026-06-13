package com.example.ucademy.dto.user;

import com.example.ucademy.dto.validation.ValueOfEnum;
import com.example.ucademy.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {
    private String firstName;
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @ValueOfEnum(enumClass = Role.class, message = "Role must be either USER or ADMIN")
    private String role;
}
