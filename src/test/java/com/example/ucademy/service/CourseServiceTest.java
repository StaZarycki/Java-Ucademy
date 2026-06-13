package com.example.ucademy.service;

import com.example.ucademy.dto.course.CourseGradesResponseDto;
import com.example.ucademy.dto.course.CourseProgressResponseDto;
import com.example.ucademy.dto.course.CourseResponseDto;
import com.example.ucademy.dto.course.CreateCourseDto;
import com.example.ucademy.model.*;
import com.example.ucademy.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseProgressRepository courseProgressRepository;
    @Mock
    private CourseGradeRepository courseGradesRepository;
    @Mock
    private CourseCertificateRepository courseCertificateRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createCourse_Success() {
        CreateCourseDto dto = new CreateCourseDto();
        dto.setCourseName("Java 101");

        Course course = new Course();
        course.setCourseName("Java 101");

        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponseDto result = courseService.createCourse(dto);

        assertNotNull(result);
        assertEquals("Java 101", result.getCourseName());
        verify(courseRepository, times(2)).save(any(Course.class)); // The service calls save twice in createCourse
    }

    @Test
    void getAllCourses_ReturnsList() {
        Course course = new Course();
        course.setCourseName("Spring Boot");
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<CourseResponseDto> result = courseService.getAllCourses();

        assertEquals(1, result.size());
        assertEquals("Spring Boot", result.get(0).getCourseName());
    }

    @Test
    void getCourseProgress_Success() {
        String email = "test@example.com";
        Long courseId = 1L;
        
        User user = new User();
        Course course = new Course();
        course.setId(courseId);
        course.setCourseName("Java");
        
        CourseProgress progress = new CourseProgress();
        progress.setCourse(course);
        progress.setProgressPercentage(50);
        progress.setStatus(Status.IN_PROGRESS);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(progress));

        CourseProgressResponseDto result = courseService.getCourseProgress(email, courseId);

        assertEquals("Java", result.getCourseName());
        assertEquals(50, result.getProgress());
        assertEquals("IN_PROGRESS", result.getStatus());
    }

    @Test
    void getCourseGrades_Success() {
        String email = "test@example.com";
        Long courseId = 1L;
        
        User user = new User();
        Course course = new Course();
        CourseGrade grade = new CourseGrade();
        grade.setGrade(90);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(new CourseProgress()));
        when(courseGradesRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(List.of(grade));

        CourseGradesResponseDto result = courseService.getCourseGrades(email, courseId);

        assertEquals(1, result.getGrades().size());
        assertEquals(90, result.getGrades().get(0));
    }

    @Test
    void getCourseGrades_NoGrades_ThrowsException() {
        String email = "test@example.com";
        Long courseId = 1L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(new Course()));
        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(new CourseProgress()));
        when(courseGradesRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> courseService.getCourseGrades(email, courseId));
    }

    @Test
    void enrollUserToCourse_Success() {
        String email = "test@example.com";
        Long courseId = 1L;
        User user = spy(new User());
        Course course = new Course();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.empty());

        courseService.enrollUserToCourse(email, courseId);

        verify(user).enrollInCourse(course);
        verify(userRepository).save(user);
    }

    @Test
    void enrollUserToCourse_AlreadyEnrolled_ThrowsException() {
        String email = "test@example.com";
        Long courseId = 1L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(new Course()));
        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(new CourseProgress()));

        assertThrows(IllegalArgumentException.class, () -> courseService.enrollUserToCourse(email, courseId));
    }

    @Test
    void updateProgress_To100Percent_SetsDone() {
        String email = "test@example.com";
        Long courseId = 1L;
        CourseProgress progress = new CourseProgress();
        progress.setStatus(Status.IN_PROGRESS);

        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(progress));

        courseService.updateProgress(email, courseId, 100);

        assertEquals(Status.DONE, progress.getStatus());
        assertNotNull(progress.getCompletedAt());
        verify(courseProgressRepository).save(progress);
    }

    @Test
    void updateProgress_Below100Percent_SetsInProgress() {
        String email = "test@example.com";
        Long courseId = 1L;
        CourseProgress progress = new CourseProgress();
        progress.setStatus(Status.DONE);

        when(courseProgressRepository.findByUserEmailAndCourseId(email, courseId)).thenReturn(Optional.of(progress));

        courseService.updateProgress(email, courseId, 50);

        assertEquals(Status.IN_PROGRESS, progress.getStatus());
        assertNull(progress.getCompletedAt());
        verify(courseProgressRepository).save(progress);
    }

    @Test
    void updateProgress_InvalidPercentage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> courseService.updateProgress("e", 1L, 101));
        assertThrows(IllegalArgumentException.class, () -> courseService.updateProgress("e", 1L, -1));
    }

    @Test
    void addCourseGrade_Success() {
        String email = "test@example.com";
        Long courseId = 1L;
        User user = new User();
        Course course = new Course();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.addCourseGrade(email, courseId, 85);

        verify(courseGradesRepository).save(any(CourseGrade.class));
    }

    @Test
    void addCourseGrade_InvalidGrade_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> courseService.addCourseGrade("e", 1L, 101));
    }

    @Test
    void issueCertificate_Success() {
        String email = "test@example.com";
        Long courseId = 1L;
        User user = new User();
        Course course = new Course();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.issueCertificate(email, courseId);

        verify(courseCertificateRepository).save(any(CourseCertificate.class));
    }
}
