package com.example.ucademy.dto.user;

import com.example.ucademy.dto.course.CourseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCertificatesResponseDto {
    private List<CourseResponseDto> courses;
}
