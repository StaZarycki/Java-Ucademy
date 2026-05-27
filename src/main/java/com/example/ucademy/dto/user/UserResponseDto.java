package com.example.ucademy.dto.user;

import com.example.ucademy.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
