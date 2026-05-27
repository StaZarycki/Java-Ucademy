package com.example.ucademy.repository;

import com.example.ucademy.model.Course;
import com.example.ucademy.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Integer> {
    Optional<CourseProgress> findByUserEmailAndCourseId(String email, Long courseId);
}
