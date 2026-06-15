package com.project.kineai.test;

import com.project.kineai.dto.request.CreateKineRequest;
import com.project.kineai.dto.request.CreatePatientRequest;
import com.project.kineai.dto.request.LoginRequest;
import com.project.kineai.dto.response.AuthResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.model.entity.User;
import com.project.kineai.model.enums.Role;
import com.project.kineai.repository.UserRepository;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.KineRepository;
import com.project.kineai.security.Jwt.JwtUtils;
import com.project.kineai.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // ── Mocks ─────────────────────────────────
    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private KineRepository kineRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    // ── Service à tester ──────────────────────
    @InjectMocks
    private AuthService authService;

    // ══════════════════════════════════════════
    // TESTS LOGIN
    // ══════════════════════════════════════════

    @Test
    @DisplayName("login — credentials valides — retourne AuthResponse")
    void login_validCredentials_returnsAuthResponse() {

        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("patient@kineai.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("patient@kineai.com")
                .role(Role.PATIENT)
                .active(true)
                .build();

        when(userRepository
                .findByEmailAndActiveTrue(request.getEmail()))
                .thenReturn(Optional.of(user));

        // ✅ 2 arguments
        when(jwtUtils.generateAccessToken(
                anyString(), anyString()))
                .thenReturn("access_token");

        when(jwtUtils.generateRefreshToken(anyString()))
                .thenReturn("refresh_token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("PATIENT", response.getRole());
    }

    @Test
    @DisplayName("login — email inexistant — lève BusinessException")
    void login_emailNotFound_throwsBusinessException() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "inconnu@kineai.com", "password123");

        when(userRepository
                .findByEmailAndActiveTrue(request.getEmail()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> authService.login(request));
    }

    // ══════════════════════════════════════════
    // TESTS REGISTER PATIENT
    // ══════════════════════════════════════════

    @Test
    @DisplayName("registerPatient — email existant — lève BusinessException")
    void registerPatient_emailExists_throwsBusinessException() {
        // Arrange
        when(userRepository
                .existsByEmail("patient@kineai.com"))
                .thenReturn(true);

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> authService.registerPatient(
                        CreatePatientRequest.builder()
                                .email("patient@kineai.com")
                                .password("password123")
                                .build()));
    }

    @Test
    @DisplayName("registerPatient — kiné introuvable — lève BusinessException")
    void registerPatient_kineNotFound_throwsBusinessException() {
        // Arrange
        when(userRepository.existsByEmail(any()))
                .thenReturn(false);
        when(kineRepository.findById(any()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> authService.registerPatient(
                        CreatePatientRequest.builder()
                                .email("patient@kineai.com")
                                .password("password123")
                                .kineId(UUID.randomUUID())
                                .build()));
    }

    // ══════════════════════════════════════════
    // TESTS REGISTER KINÉ
    // ══════════════════════════════════════════

    @Test
    @DisplayName("registerKine — email existant — lève BusinessException")
    void registerKine_emailExists_throwsBusinessException() {
        // Arrange
        when(userRepository
                .existsByEmail("kine@kineai.com"))
                .thenReturn(true);

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> authService.registerKine(
                        CreateKineRequest.builder()
                                .email("kine@kineai.com")
                                .password("password123")
                                .build()));
    }

    // ══════════════════════════════════════════
    // TESTS REFRESH TOKEN
    // ══════════════════════════════════════════

    @Test
    @DisplayName("refresh — token valide — retourne nouveaux tokens")
    void refresh_validToken_returnsNewTokens() {

        // Arrange
        String refreshToken = "valid_refresh_token";
        String email = "patient@kineai.com";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(Role.PATIENT)
                .active(true)
                .build();

        // ✅ Noms corrects de vos méthodes
        when(jwtUtils.getEmailFromToken(refreshToken))
                .thenReturn(email);
        when(jwtUtils.validateToken(refreshToken))
                .thenReturn(true);
        when(userRepository
                .findByEmailAndActiveTrue(email))
                .thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(email, "PATIENT"))
                .thenReturn("new_access_token");
        when(jwtUtils.generateRefreshToken(email))
                .thenReturn("new_refresh_token");

        // Act
        AuthResponse response = authService.refresh(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new_access_token",
                response.getAccessToken());
    }

}