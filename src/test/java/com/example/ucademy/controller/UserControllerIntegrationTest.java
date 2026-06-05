package com.example.ucademy.controller;

import com.example.ucademy.BaseIntegrationTest;
import com.example.ucademy.dto.user.CreateUserDto;
import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("user@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser.setFirstName("Regular");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);

        userToken = createToken(testUser);
    }

    @Test
    void createUser_Success() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        ReflectionTestUtils.setField(dto, "firstName", "New");
        ReflectionTestUtils.setField(dto, "lastName", "User");
        ReflectionTestUtils.setField(dto, "email", "new@example.com");
        ReflectionTestUtils.setField(dto, "password", "password123");
        ReflectionTestUtils.setField(dto, "role", "USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void getCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setRole(Role.USER);
        userRepository.save(anotherUser);

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
