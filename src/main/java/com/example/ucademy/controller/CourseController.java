package com.example.ucademy.controller;

import com.example.ucademy.dto.course.CourseGradesResponseDto;
import com.example.ucademy.dto.course.CourseProgressResponseDto;
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
            @PathVariable Long courseId,
            @AuthenticationPrincipal String email
    ) {
        courseService.enrollUserToCourse(email, courseId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully enrolled to the course");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{courseId}/progress")
    @Operation(method = "PATCH", summary = "Update progress", description = "Update course progress")
    public ResponseEntity<Map<String, String>> updateCourseProgress(
            @PathVariable Long courseId,
            @RequestParam("percentage") int percentage,
            @AuthenticationPrincipal String email
    ) {
        courseService.updateProgress(email, courseId, percentage);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully updated progress");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/progress")
    @Operation(method = "GET", summary = "Get progress", description = "Get current course progress")
    public ResponseEntity<CourseProgressResponseDto> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal String email
    ) {
        CourseProgressResponseDto response = courseService.getCourseProgress(email, courseId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/grades")
    @Operation(method = "GET", summary = "Get grades", description = "Get grades for the current course")
    public ResponseEntity<CourseGradesResponseDto> getCourseGrades(
            @PathVariable Long courseId,
            @AuthenticationPrincipal String email
    ) {
        CourseGradesResponseDto response = courseService.getCourseGrades(email, courseId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{courseId}/grades")
    @Operation(method = "POST", summary = "Add grade", description = "Add new grade to the course")
    public ResponseEntity<Void> addGrade(
            @PathVariable Long courseId,
            @AuthenticationPrincipal String email,
            @RequestParam("grade") int grade
    ) {
        courseService.addCourseGrade(email, courseId, grade);

        return new  ResponseEntity<>(HttpStatus.CREATED);
    }
}
