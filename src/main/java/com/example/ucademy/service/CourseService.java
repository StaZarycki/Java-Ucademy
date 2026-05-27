package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.Course;
import com.example.ucademy.model.User;
import com.example.ucademy.repository.CourseRepository;
import com.example.ucademy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto responseDto = new CourseResponseDto();
        responseDto.setCourseName(course.getCourseName());

        return responseDto;
    }

    public CourseService(CourseRepository courseRepository,  UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
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

        if (user.getCourses().contains(course)) {
            throw new IllegalArgumentException("User already enrolled");
        }

        user.addCourse(course);
    }
}
