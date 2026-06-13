package com.example.ucademy.service;

import com.example.ucademy.dto.user.CreateUserDto;
import com.example.ucademy.dto.user.UserCertificatesResponseDto;
import com.example.ucademy.dto.user.UserResponseDto;
import com.example.ucademy.model.Course;
import com.example.ucademy.model.CourseCertificate;
import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.CourseCertificateRepository;
import com.example.ucademy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CourseCertificateRepository courseCertificateRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPassword("password");
        dto.setRole("USER");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");

        UserResponseDto response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals(dto.getEmail(), response.getEmail());
        assertEquals(dto.getFirstName(), response.getFirstName());
        assertEquals(Role.USER, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithNullRole_Success() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole(null);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");

        UserResponseDto response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals(Role.USER, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_AlreadyExists_ThrowsException() {
        CreateUserDto dto = new CreateUserDto();
        dto.setEmail("exists@example.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ReturnsList() {
        User user = new User();
        user.setEmail("user@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("user@example.com", result.get(0).getEmail());
    }

    @Test
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(1L));
    }

    @Test
    void deleteUserById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserById_NotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserById(1L));
    }

    @Test
    void getCurrentUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getCurrentUser("test@example.com");

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getCurrentUser_NotFound_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getCurrentUser("test@example.com"));
    }

    @Test
    void getUserCertificates_ReturnsDto() {
        String email = "test@example.com";
        Course course = new Course();
        course.setCourseName("Java Course");
        
        CourseCertificate certificate = new CourseCertificate();
        certificate.setCourse(course);
        
        when(courseCertificateRepository.findAllByUserEmail(email)).thenReturn(List.of(certificate));

        UserCertificatesResponseDto result = userService.getUserCertificates(email);

        assertNotNull(result);
        assertEquals(1, result.getCourses().size());
        assertEquals("Java Course", result.getCourses().get(0).getCourseName());
    }
}
