package com.example.ucademy.dto.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourseDto {
    @NotBlank
    private String courseName;
}
