package com.example.ucademy.repository;

import com.example.ucademy.model.CourseCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Long> {
    List<CourseCertificate> findAllByUserEmail(String email);
}
