package com.example.ucademy.controller;

import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course", description = "Course Controller")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(method = "POST", summary = "Create course", description = "Create a new course")
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CreateCourseDto dto) {
        CourseResponseDto response = courseService.createCourse(dto);
        return new  ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(method = "GET", summary = "Get all courses", description = "Get all courses from the database")
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> response = courseService.getAllCourses();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{courseId}/enroll")
    @Operation(method = "POST", summary = "Enroll", description = "Enroll currently logged in user to the course")
    public ResponseEntity<Map<String, String>> enrollToCourse(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal String email
    ) {
        courseService.enrollUserToCourse(email, courseId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully enrolled to the course");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
