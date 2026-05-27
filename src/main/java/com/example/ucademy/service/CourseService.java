package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.Course;
import com.example.ucademy.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto responseDto = new CourseResponseDto();
        responseDto.setCourseName(course.getCourseName());

        return responseDto;
    }

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
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
}
