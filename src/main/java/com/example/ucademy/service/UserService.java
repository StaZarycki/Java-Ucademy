package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.user.CreateUserDto;
import com.example.ucademy.dto.user.UserCertificatesResponseDto;
import com.example.ucademy.dto.user.UserResponseDto;
import com.example.ucademy.model.CourseCertificate;
import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.CourseCertificateRepository;
import com.example.ucademy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseCertificateRepository  courseCertificateRepository;

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        responseDto.setEmail(user.getEmail());
        responseDto.setRole(user.getRole());

        return responseDto;
    }

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CourseCertificateRepository courseCertificateRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseCertificateRepository = courseCertificateRepository;
    }

    public UserResponseDto createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Account with this email already exists!");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);

        return mapToResponseDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with this id does not exist"));

        return mapToResponseDto(user);
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with this id does not exist");
        }

        userRepository.deleteById(id);
    }

    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user context not found"));

        return mapToResponseDto(user);
    }

    public UserCertificatesResponseDto getUserCertificates(String email) {
        List<CourseCertificate> certificates = courseCertificateRepository.findAllByUserEmail(email);

        UserCertificatesResponseDto responseDto = new UserCertificatesResponseDto();
        responseDto.setCourses(certificates
                .stream()
                .map(CourseCertificate::getCourse)
                        .map(course -> {
                            CourseResponseDto courseResponseDto = new CourseResponseDto();
                            courseResponseDto.setCourseName(course.getCourseName());

                            return courseResponseDto;
                        })
                .collect(Collectors.toList()));

        return responseDto;
    }
}
