package com.project.kineai.test;

import com.project.kineai.dto.request.CompleteSessionRequest;
import com.project.kineai.dto.request.CreateSessionRequest;
import com.project.kineai.dto.response.SessionResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.mapper.SessionMapper;
import com.project.kineai.mapper.SessionMetricsMapper;
import com.project.kineai.model.entity.*;
import com.project.kineai.model.enums.SessionStatus;
import com.project.kineai.repository.*;
import com.project.kineai.service.BadgeService;
import com.project.kineai.service.PatientService;
import com.project.kineai.service.RehabPlanService;
import com.project.kineai.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    // ── Mocks ─────────────────────────────────
    @Mock private SessionRepository sessionRepository;
    @Mock private SessionMetricsRepository metricsRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private RehabPlanRepository planRepository;
    @Mock private SessionMapper sessionMapper;
    @Mock private SessionMetricsMapper metricsMapper;
    @Mock private PatientService patientService;
    @Mock private BadgeService badgeService;
    @Mock private RehabPlanService rehabPlanService;

    // ── Service à tester ──────────────────────
    @InjectMocks
    private SessionService sessionService;

    // ══════════════════════════════════════════
    // TESTS startSession
    // ══════════════════════════════════════════

    @Test
    @DisplayName("startSession — exercice introuvable — lève BusinessException")
    void startSession_exerciseNotFound_throwsBusinessException() {
        // Arrange
        CreateSessionRequest request = CreateSessionRequest
                .builder()
                .exerciseId(UUID.randomUUID())
                .build();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .build();

        when(patientService.getCurrentPatient())
                .thenReturn(patient);
        when(exerciseRepository.findById(request.getExerciseId()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> sessionService.startSession(request));
    }

    @Test
    @DisplayName("startSession — succès — statut IN_PROGRESS")
    void startSession_validRequest_statusInProgress() {
        // Arrange
        UUID exerciseId = UUID.randomUUID();
        CreateSessionRequest request = CreateSessionRequest
                .builder()
                .exerciseId(exerciseId)
                .build();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .build();

        Exercise exercise = Exercise.builder()
                .id(exerciseId)
                .name("Flexion genou")
                .build();

        Session savedSession = Session.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .exercise(exercise)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        when(patientService.getCurrentPatient())
                .thenReturn(patient);
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));
        when(sessionRepository.save(any()))
                .thenReturn(savedSession);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.startSession(request);

        // Assert — statut IN_PROGRESS à la création
        verify(sessionRepository).save(argThat(session ->
                session.getSessionStatus() == SessionStatus.IN_PROGRESS));
    }

    // ══════════════════════════════════════════
    // TESTS completeSession
    // ══════════════════════════════════════════

    @Test
    @DisplayName("completeSession — séance introuvable — lève BusinessException")
    void completeSession_sessionNotFound_throwsBusinessException() {
        // Arrange
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> sessionService.completeSession(
                        sessionId,
                        CompleteSessionRequest.builder()
                                .finalScore(BigDecimal.valueOf(80.0))
                                .repsCompleted(10)
                                .build()));
    }

    @Test
    @DisplayName("completeSession — score > 80% — XP = 30 (RG-29)")
    void completeSession_scoreAbove80_xpIs30() {
        // Arrange
        UUID sessionId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .totalXp(0)
                .streakCount(0)
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        CompleteSessionRequest request = CompleteSessionRequest
                .builder()
                .finalScore(BigDecimal.valueOf(85.0))
                .repsCompleted(10)
                .jointAngles("{}")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.completeSession(sessionId, request);

        // Assert — XP = 10 base + 20 bonus = 30 (RG-29)
        verify(sessionRepository).save(argThat(s ->
                s.getXpEarned() == 30));
    }

    @Test
    @DisplayName("completeSession — score <= 80% — XP = 10 (RG-29)")
    void completeSession_scoreBelow80_xpIs10() {
        // Arrange
        UUID sessionId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .totalXp(0)
                .streakCount(0)
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        CompleteSessionRequest request = CompleteSessionRequest
                .builder()
                .finalScore(BigDecimal.valueOf(75.0))
                .repsCompleted(8)
                .jointAngles("{}")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.completeSession(sessionId, request);

        // Assert — XP = 10 uniquement (RG-29)
        verify(sessionRepository).save(argThat(s ->
                s.getXpEarned() == 10));
    }

    @Test
    @DisplayName("completeSession — statut COMPLETED après completion")
    void completeSession_success_statusCompleted() {
        // Arrange
        UUID sessionId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .totalXp(0)
                .streakCount(0)
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        CompleteSessionRequest request = CompleteSessionRequest
                .builder()
                .finalScore(BigDecimal.valueOf(80.0))
                .repsCompleted(10)
                .jointAngles("{}")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.completeSession(sessionId, request);

        // Assert — statut COMPLETED (RG-13)
        verify(sessionRepository).save(argThat(s ->
                s.getSessionStatus() == SessionStatus.COMPLETED));
    }

    @Test
    @DisplayName("completeSession — badges vérifiés après completion (RG-30)")
    void completeSession_success_badgesChecked() {
        // Arrange
        UUID sessionId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .totalXp(0)
                .streakCount(0)
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        CompleteSessionRequest request = CompleteSessionRequest
                .builder()
                .finalScore(BigDecimal.valueOf(90.0))
                .repsCompleted(12)
                .jointAngles("{}")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.completeSession(sessionId, request);

        // Assert — BadgeService appelé après completion
        verify(badgeService).checkAndUnlockBadges(
                any(Patient.class), any(Session.class));
    }

    @Test
    @DisplayName("completeSession — progression vérifiée après completion (RG-19/RG-20)")
    void completeSession_success_progressionChecked() {
        // Arrange
        UUID sessionId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(patientId)
                .totalXp(0)
                .streakCount(0)
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        CompleteSessionRequest request = CompleteSessionRequest
                .builder()
                .finalScore(BigDecimal.valueOf(90.0))
                .repsCompleted(12)
                .jointAngles("{}")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.completeSession(sessionId, request);

        // Assert — checkProgression appelé (RG-19/RG-20)
        verify(rehabPlanService).checkProgression(patientId);
    }

    // ══════════════════════════════════════════
    // TESTS interruptSession
    // ══════════════════════════════════════════

    @Test
    @DisplayName("interruptSession — séance introuvable — lève BusinessException")
    void interruptSession_sessionNotFound_throwsBusinessException() {
        // Arrange
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> sessionService.interruptSession(sessionId));
    }

    @Test
    @DisplayName("interruptSession — statut INTERRUPTED — aucun XP (RG-14)")
    void interruptSession_success_statusInterrupted() {
        // Arrange
        UUID sessionId = UUID.randomUUID();

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .build();

        Session session = Session.builder()
                .id(sessionId)
                .patient(patient)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any()))
                .thenReturn(session);
        when(sessionMapper.toResponse(any()))
                .thenReturn(new SessionResponse());

        // Act
        sessionService.interruptSession(sessionId);

        // Assert — statut INTERRUPTED (RG-13)
        verify(sessionRepository).save(argThat(s ->
                s.getSessionStatus() == SessionStatus.INTERRUPTED));

        // Assert — BadgeService jamais appelé (RG-14)
        verify(badgeService, never())
                .checkAndUnlockBadges(any(), any());
    }
}