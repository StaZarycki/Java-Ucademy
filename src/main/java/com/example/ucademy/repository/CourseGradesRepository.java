package com.example.ucademy.repository;

import com.example.ucademy.model.CourseGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseGradesRepository extends JpaRepository<CourseGrade, Long> {
    List<CourseGrade> findByUserEmailAndCourseId(String email, Long courseId);
}
