package com.example.ucademy.controller;

import com.example.ucademy.BaseIntegrationTest;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.Course;
import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.CourseRepository;
import com.example.ucademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser = userRepository.save(adminUser);
        adminToken = createToken(adminUser);

        regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setRole(Role.USER);
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser = userRepository.save(regularUser);
        userToken = createToken(regularUser);
    }

    @Test
    void getAllCourses_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        courseRepository.save(course);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseName", is("Java Programming")));
    }

    @Test
    void createCourse_AsAdmin_Success() throws Exception {
        CreateCourseDto dto = new CreateCourseDto();
        dto.setCourseName("New Course");

        mockMvc.perform(post("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseName", is("New Course")));
    }

    @Test
    void createCourse_AsUser_Forbidden() throws Exception {
        CreateCourseDto dto = new CreateCourseDto();
        dto.setCourseName("New Course");

        mockMvc.perform(post("/api/courses")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void enrollToCourse_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        course = courseRepository.save(course);

        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully enrolled to the course")));
    }

    @Test
    void updateCourseProgress_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        course = courseRepository.save(course);

        // First enroll
        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/courses/" + course.getId() + "/progress")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .param("percentage", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Successfully updated progress")));
    }

    @Test
    void updateCourseProgress_InvalidPercentage_BadRequest() throws Exception {
        mockMvc.perform(patch("/api/courses/1/progress")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .param("percentage", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid percentage (must be between 0 and 100)")));
    }

    @Test
    void getCourseProgress_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        course = courseRepository.save(course);

        // First enroll
        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/courses/" + course.getId() + "/progress")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName", is("Java Programming")))
                .andExpect(jsonPath("$.progress", is(0)));
    }

    @Test
    void getCourseGrades_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        course = courseRepository.save(course);

        // Enroll and add grade
        mockMvc.perform(post("/api/courses/" + course.getId() + "/enroll")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/" + course.getId() + "/grades")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .param("grade", "95"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/courses/" + course.getId() + "/grades")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grades", hasSize(1)))
                .andExpect(jsonPath("$.grades[0]", is(95)));
    }

    @Test
    void issueCertificate_Success() throws Exception {
        Course course = new Course();
        course.setCourseName("Java Programming");
        course = courseRepository.save(course);

        mockMvc.perform(post("/api/courses/" + course.getId() + "/issueCertificate")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("userEmail", regularUser.getEmail()))
                .andExpect(status().isCreated());
    }
}
