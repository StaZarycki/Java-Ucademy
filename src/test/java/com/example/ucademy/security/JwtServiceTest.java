package com.example.ucademy.security;

import com.example.ucademy.model.Role;
import com.example.ucademy.model.User;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L);
        
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
    }

    @Test
    void generateToken_Success() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_Success() {
        String token = jwtService.generateToken(testUser);
        String email = jwtService.extractEmail(token);
        assertEquals(testUser.getEmail(), email);
    }

    @Test
    void extractRole_Success() {
        String token = jwtService.generateToken(testUser);
        String role = jwtService.extractRole(token);
        assertEquals(Role.USER.name(), role);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser.getEmail());
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_DifferentEmail_ReturnsFalse() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, "other@example.com");
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Use the internal getSigningKey to ensure we use the same key for manual token creation
        SecretKey key = (SecretKey) ReflectionTestUtils.invokeMethod(jwtService, "getSigningKey");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", Role.USER.name());

        String expiredToken = Jwts.builder()
                .claims(claims)
                .subject(testUser.getEmail())
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key)
                .compact();
        
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, testUser.getEmail()));
    }
}
