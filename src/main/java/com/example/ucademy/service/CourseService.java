package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.Course;
import com.example.ucademy.model.CourseProgress;
import com.example.ucademy.model.Status;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.CourseProgressRepository;
import com.example.ucademy.repository.CourseRepository;
import com.example.ucademy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseProgressRepository courseProgressRepository;

    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto responseDto = new CourseResponseDto();
        responseDto.setCourseName(course.getCourseName());

        return responseDto;
    }

    public CourseService(CourseRepository courseRepository, UserRepository userRepository, CourseProgressRepository courseProgressRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseProgressRepository = courseProgressRepository;
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
}
