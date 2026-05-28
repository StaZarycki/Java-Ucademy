package com.example.ucademy.dto.course;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class CourseGradesResponseDto {
    private Long courseId;
    private ArrayList<Integer> grades;
}
