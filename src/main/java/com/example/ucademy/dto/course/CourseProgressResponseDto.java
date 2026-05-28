package com.example.ucademy.dto.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseProgressResponseDto {
    private Long courseId;
    private String courseName;
    private String status;
    private int progress;
}
