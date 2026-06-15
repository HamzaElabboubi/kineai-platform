package com.project.kineai.test;

import com.project.kineai.dto.request.CreatePlanRequest;
import com.project.kineai.dto.response.RehabPlanResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.mapper.RehabPlanMapper;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Status;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.RehabPlanRepository;
import com.project.kineai.repository.SessionRepository;
import com.project.kineai.service.PatientService;
import com.project.kineai.service.RehabPlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RehabPlanServiceTest {

    // ── Mocks ─────────────────────────────────
    @Mock private RehabPlanRepository planRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private RehabPlanMapper planMapper;
    @Mock private PatientService patientService;

    // ── Service à tester ──────────────────────
    @InjectMocks
    private RehabPlanService rehabPlanService;

    // ══════════════════════════════════════════
    // TESTS generatePlan
    // ══════════════════════════════════════════

    @Test
    @DisplayName("generatePlan — patient introuvable — lève BusinessException")
    void generatePlan_patientNotFound_throwsBusinessException() {
        // Arrange
        CreatePlanRequest request = CreatePlanRequest.builder()
                .patientId(UUID.randomUUID())
                .startDate(LocalDate.now())
                .build();

        when(patientRepository.findById(request.getPatientId()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> rehabPlanService.generatePlan(request));
    }

    @Test
    @DisplayName("generatePlan — patient age >= 65 — niveau DÉBUTANT (RG-39)")
    void generatePlan_patientAge65_levelDebutant() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .age(70)
                .level(Level.INTERMEDIAIRE)
                .build();

        CreatePlanRequest request = CreatePlanRequest.builder()
                .patientId(patientId)
                .startDate(LocalDate.now())
                .build();

        RehabPlan savedPlan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(28))
                .status(Status.ACTIVE)
                .difficultyLevel(Level.DEBUTANT)
                .currentWeek(1)
                .build();

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.empty());
        when(planRepository.save(any()))
                .thenReturn(savedPlan);
        when(planMapper.toResponse(any()))
                .thenReturn(new RehabPlanResponse());

        // Act
        rehabPlanService.generatePlan(request);

        // Assert — vérifier que le plan sauvegardé
        // a le niveau DÉBUTANT (RG-39)
        verify(planRepository).save(argThat(plan ->
                plan.getDifficultyLevel() == Level.DEBUTANT));
    }

    @Test
    @DisplayName("generatePlan — endDate = startDate + 28 jours (RG-18)")
    void generatePlan_endDateIs28DaysAfterStart() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 6, 1);

        Patient patient = Patient.builder()
                .id(patientId)
                .age(30)
                .level(Level.DEBUTANT)
                .build();

        CreatePlanRequest request = CreatePlanRequest.builder()
                .patientId(patientId)
                .startDate(startDate)
                .build();

        RehabPlan savedPlan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .startDate(startDate)
                .endDate(startDate.plusDays(28))
                .status(Status.ACTIVE)
                .difficultyLevel(Level.DEBUTANT)
                .currentWeek(1)
                .build();

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.empty());
        when(planRepository.save(any()))
                .thenReturn(savedPlan);
        when(planMapper.toResponse(any()))
                .thenReturn(new RehabPlanResponse());

        // Act
        rehabPlanService.generatePlan(request);

        // Assert — endDate = startDate + 28 jours (RG-18)
        verify(planRepository).save(argThat(plan ->
                plan.getEndDate().equals(startDate.plusDays(28))));
    }

    @Test
    @DisplayName("generatePlan — plan actif existant — archivé (RG-17)")
    void generatePlan_existingActivePlan_archivedToDone() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .age(30)
                .level(Level.DEBUTANT)
                .build();

        RehabPlan existingPlan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .status(Status.ACTIVE)
                .difficultyLevel(Level.DEBUTANT)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(18))
                .currentWeek(2)
                .build();

        CreatePlanRequest request = CreatePlanRequest.builder()
                .patientId(patientId)
                .startDate(LocalDate.now())
                .build();

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.of(existingPlan));
        when(planRepository.save(any()))
                .thenReturn(RehabPlan.builder()
                        .id(UUID.randomUUID())
                        .patient(patient)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(28))
                        .status(Status.ACTIVE)
                        .difficultyLevel(Level.DEBUTANT)
                        .currentWeek(1)
                        .build());
        when(planMapper.toResponse(any()))
                .thenReturn(new RehabPlanResponse());

        // Act
        rehabPlanService.generatePlan(request);

        // Assert — plan précédent archivé (RG-17)
        assertEquals(Status.DONE, existingPlan.getStatus());
    }

    // ══════════════════════════════════════════
    // TESTS checkProgression
    // ══════════════════════════════════════════

    @Test
    @DisplayName("checkProgression — score > 85% — niveau augmenté (RG-19)")
    void checkProgression_scoreAbove85_upgradesLevel() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .age(30)
                .level(Level.DEBUTANT)
                .build();

        RehabPlan plan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .status(Status.ACTIVE)
                .difficultyLevel(Level.DEBUTANT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(28))
                .currentWeek(1)
                .build();

        when(sessionRepository
                .getAverageScoreLastThreeSessions(patientId))
                .thenReturn(90.0);
        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.of(plan));

        // Act
        rehabPlanService.checkProgression(patientId);

        // Assert — niveau augmenté DÉBUTANT → INTERMÉDIAIRE
        assertEquals(Level.INTERMEDIAIRE,
                plan.getDifficultyLevel());
        verify(planRepository).save(plan);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("checkProgression — score < 50% — niveau diminué (RG-20)")
    void checkProgression_scoreBelow50_downgradesLevel() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .age(30)
                .level(Level.INTERMEDIAIRE)
                .build();

        RehabPlan plan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .status(Status.ACTIVE)
                .difficultyLevel(Level.INTERMEDIAIRE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(28))
                .currentWeek(1)
                .build();

        when(sessionRepository
                .getAverageScoreLastThreeSessions(patientId))
                .thenReturn(40.0);
        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.of(plan));

        // Act
        rehabPlanService.checkProgression(patientId);

        // Assert — niveau diminué INTERMÉDIAIRE → DÉBUTANT
        assertEquals(Level.DEBUTANT,
                plan.getDifficultyLevel());
        verify(planRepository).save(plan);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("checkProgression — score null — aucune modification")
    void checkProgression_nullScore_noModification() {
        // Arrange
        UUID patientId = UUID.randomUUID();

        when(sessionRepository
                .getAverageScoreLastThreeSessions(patientId))
                .thenReturn(null);

        // Act
        rehabPlanService.checkProgression(patientId);

        // Assert — aucune interaction avec les autres repos
        verify(patientRepository, never()).findById(any());
        verify(planRepository, never())
                .findByPatientIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("checkProgression — score entre 50 et 85 — aucune modification")
    void checkProgression_scoreBetween50And85_noChange() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(patientId)
                .age(30)
                .level(Level.DEBUTANT)
                .build();

        RehabPlan plan = RehabPlan.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .status(Status.ACTIVE)
                .difficultyLevel(Level.DEBUTANT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(28))
                .currentWeek(1)
                .build();

        when(sessionRepository
                .getAverageScoreLastThreeSessions(patientId))
                .thenReturn(70.0);
        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.of(plan));

        // Act
        rehabPlanService.checkProgression(patientId);

        // Assert — niveau inchangé
        assertEquals(Level.DEBUTANT,
                plan.getDifficultyLevel());
        verify(planRepository, never()).save(any());
    }

    // ══════════════════════════════════════════
    // TESTS getActivePlan
    // ══════════════════════════════════════════

    @Test
    @DisplayName("getActivePlan — aucun plan actif — lève BusinessException")
    void getActivePlan_noPlanFound_throwsBusinessException() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        when(planRepository.findByPatientIdAndStatus(
                patientId, Status.ACTIVE))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(BusinessException.class,
                () -> rehabPlanService.getActivePlan(patientId));
    }
}