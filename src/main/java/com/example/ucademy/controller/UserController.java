package com.example.ucademy.controller;

import com.example.ucademy.dto.user.CreateUserDto;
import com.example.ucademy.dto.user.UserCertificatesResponseDto;
import com.example.ucademy.dto.user.UserResponseDto;
import com.example.ucademy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User Controller")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(method = "POST", summary = "Create user", description = "Create a new user")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserDto dto) {
        UserResponseDto response = userService.createUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(method = "GET", summary = "Get all users", description = "Get all users from the database")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> response = userService.getAllUsers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(method = "GET", summary = "Get user by id", description = "Get user by id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto response = userService.getUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me")
    @Operation(method = "GET", summary = "Get current user", description = "Get info about currently signed in user")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal String email) {
        UserResponseDto response = userService.getCurrentUser(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(method = "DELETE", summary = "Delete user by id", description = "Delete user by id")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/certificates")
    @Operation(method = "GET", summary = "Get current user's certificates", description = "Get current user's certificates")
    public ResponseEntity<UserCertificatesResponseDto> getCurrentUserCertificates(@AuthenticationPrincipal String email) {
        UserCertificatesResponseDto response = userService.getUserCertificates(email);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
