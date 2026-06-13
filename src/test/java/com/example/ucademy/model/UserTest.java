package com.example.ucademy.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Course course;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        
        course = new Course();
        course.setCourseName("Java Basics");
    }

    @Test
    void enrollInCourse_UpdatesBothSides() {
        user.enrollInCourse(course);

        assertEquals(1, user.getCourseProgresses().size());
        assertEquals(1, course.getCourseProgresses().size());

        CourseProgress progress = user.getCourseProgresses().iterator().next();
        assertEquals(user, progress.getUser());
        assertEquals(course, progress.getCourse());
        assertTrue(course.getCourseProgresses().contains(progress));
    }

    @Test
    void removeCourse_WhenEnrolled_UpdatesBothSides() {
        user.enrollInCourse(course);
        assertEquals(1, user.getCourseProgresses().size());

        user.removeCourse(course);

        assertEquals(0, user.getCourseProgresses().size());
        assertEquals(0, course.getCourseProgresses().size());
    }

    @Test
    void removeCourse_WhenNotEnrolled_DoesNothing() {
        Course otherCourse = new Course();
        otherCourse.setCourseName("Other");
        
        user.enrollInCourse(course);
        int userInitialSize = user.getCourseProgresses().size();
        int courseInitialSize = otherCourse.getCourseProgresses().size();

        user.removeCourse(otherCourse);

        assertEquals(userInitialSize, user.getCourseProgresses().size());
        assertEquals(courseInitialSize, otherCourse.getCourseProgresses().size());
    }
}
