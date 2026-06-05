package com.example.ucademy.controller;

import com.example.ucademy.BaseIntegrationTest;
import com.example.ucademy.dto.LoginRequestDto;
import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    User user = new User();
    user.setEmail("test@example.com");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole(Role.USER);
    user.setFirstName("Test");
    user.setLastName("User");
    userRepository.save(user);
  }

  @Test
  void login_Success() throws Exception {
    LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()));
  }

  @Test
  void login_Failure_InvalidCredentials() throws Exception {
    LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrongpassword");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_Failure_NonExistentUser() throws Exception {
    LoginRequestDto loginRequest = new LoginRequestDto("nonexistent@example.com", "password123");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
  }
}
