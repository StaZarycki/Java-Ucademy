package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseGradesResponseDto;
import com.example.ucademy.dto.course.CourseProgressResponseDto;
import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.*;
import com.example.ucademy.repository.CourseGradesRepository;
import com.example.ucademy.repository.CourseProgressRepository;
import com.example.ucademy.repository.CourseRepository;
import com.example.ucademy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseGradesRepository courseGradesRepository;

    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto responseDto = new CourseResponseDto();
        responseDto.setCourseName(course.getCourseName());

        return responseDto;
    }

    private CourseProgressResponseDto mapToProgressResponseDto(CourseProgress courseProgress) {
        CourseProgressResponseDto responseDto = new CourseProgressResponseDto();
        responseDto.setCourseId(courseProgress.getCourse().getId());
        responseDto.setCourseName(courseProgress.getCourse().getCourseName());
        responseDto.setProgress(courseProgress.getProgressPercentage());
        responseDto.setStatus(courseProgress.getStatus().toString());

        return responseDto;
    }

    private void validateUserEnrollment(String email, Long courseId) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        boolean enrolled = courseProgressRepository.findByUserEmailAndCourseId(email, courseId).isPresent();
        if (!enrolled) {
            throw new IllegalArgumentException("User is not enrolled to the course");
        }
    }

    public CourseService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            CourseProgressRepository courseProgressRepository,
            CourseGradesRepository courseGradesRepository
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.courseGradesRepository = courseGradesRepository;
    }

    public CourseResponseDto createCourse(CreateCourseDto dto) {
        Course course = new Course();
        course.setCourseName(dto.getCourseName());

        courseRepository.save(course);

        return mapToResponseDto(courseRepository.save(course));
    }

    public List<CourseResponseDto> getAllCourses() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    public CourseProgressResponseDto getCourseProgress(String email, Long courseId) {
        validateUserEnrollment(email, courseId);

        CourseProgress courseProgress = courseProgressRepository.findByUserEmailAndCourseId(email, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course progress not found"));

        return mapToProgressResponseDto(courseProgress);
    }

    public CourseGradesResponseDto getCourseGrades(String email, Long courseId) {
        validateUserEnrollment(email, courseId);

        List<CourseGrade> courseGrades = courseGradesRepository.findByUserEmailAndCourseId(email, courseId);
        if (courseGrades.isEmpty()) {
            throw new IllegalArgumentException("User does not have any grades within the course");
        }

        CourseGradesResponseDto responseDto = new CourseGradesResponseDto();
        responseDto.setCourseId(courseId);
        responseDto.setGrades(new ArrayList<>(courseGrades
                .stream()
                .map(CourseGrade::getGrade)
                .collect(Collectors.toList())));

        return responseDto;
    }

    @Transactional
    public void enrollUserToCourse(String email, Long courseId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        boolean alreadyEnrolled = courseProgressRepository.findByUserEmailAndCourseId(email, courseId).isPresent();
        if (alreadyEnrolled) {
            throw new IllegalArgumentException("User already enrolled");
        }

        user.enrollInCourse(course);
        userRepository.save(user);
    }

    @Transactional
    public void updateProgress(String email, Long courseId, int newPercentage) {
        if (newPercentage < 0 || newPercentage > 100) {
            throw new IllegalArgumentException("Invalid percentage (must be between 0 and 100)");
        }

        CourseProgress progress = courseProgressRepository.findByUserEmailAndCourseId(email, courseId)
                .orElseThrow(() -> new IllegalArgumentException("User is not enrolled in the course"));

        progress.setProgressPercentage(newPercentage);

        if (newPercentage == 100) {
            progress.setStatus(Status.DONE);
            progress.setCompletedAt(LocalDateTime.now());
        } else {
            progress.setStatus(Status.IN_PROGRESS);
            progress.setCompletedAt(null);
        }

        courseProgressRepository.save(progress);
    }

    @Transactional
    public void addCourseGrade(String email, Long courseId, int grade) {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Invalid grade (must be between 0 and 100)");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        CourseGrade courseGrade = new CourseGrade();
        courseGrade.setCourse(course);
        courseGrade.setUser(user);
        courseGrade.setGrade(grade);

        courseGradesRepository.save(courseGrade);
    }
}
