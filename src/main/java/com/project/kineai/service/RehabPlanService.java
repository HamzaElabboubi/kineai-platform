package com.project.kineai.service;


import com.project.kineai.dto.request.CreatePlanRequest;
import com.project.kineai.dto.response.RehabPlanResponse;
import com.project.kineai.mapper.RehabPlanMapper;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Status;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.RehabPlanRepository;
import com.project.kineai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RehabPlanService {

    private final RehabPlanRepository planRepository;
    private final PatientRepository patientRepository;
    private final SessionRepository sessionRepository;
    private final RehabPlanMapper planMapper;

    // ── Générer plan — Système Expert ─────────
    @Transactional
    public RehabPlanResponse generatePlan(CreatePlanRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException("Patient introuvable"));

        //  archiver plan actif existant
        planRepository.findByPatientIdAndStatus(patient.getId(), Status.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(Status.DONE);
                    planRepository.save(existing);
                });

        // Système expert — déterminer niveau initial
        Level level = determineLevel(patient);

        // Dates du plan
        LocalDate start = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        RehabPlan plan = RehabPlan.builder()
                .patient(patient)
                .startDate(start)
                .endDate(start.plusDays(28)) // 4 semaines
                .status(Status.ACTIVE)
                .difficultyLevel(level)
                .currentWeek(1)
                .build();

        plan = planRepository.save(plan);
        log.info("Plan généré pour patient {} — niveau {}", patient.getId(), level);
        return planMapper.toResponse(plan);
    }
    // ── Règle RG-19/RG-20 ────────────────────
    @Transactional
    public void checkProgression(UUID patientId) {
        Double avgScore = sessionRepository
                .getAverageScoreLastThreeSessions(patientId);
        if (avgScore == null) return;

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("Patient introuvable"));

        planRepository.findByPatientIdAndStatus(patientId, Status.ACTIVE)
                .ifPresent(plan -> {
                    // RG-19 — Score > 85% → progression
                    if (avgScore > 85.0 && plan.getDifficultyLevel() != Level.AVANCE) {
                        Level newLevel = upgradeLevel(plan.getDifficultyLevel());
                        plan.setDifficultyLevel(newLevel);
                        patient.setLevel(newLevel);
                        planRepository.save(plan);
                        patientRepository.save(patient);
                        log.info("Progression patient {} → {}", patientId, newLevel);
                    }
                    // RG-20 — Score < 50% → régression
                    else if (avgScore < 50.0 && plan.getDifficultyLevel() != Level.DEBUTANT) {
                        Level newLevel = downgradeLevel(plan.getDifficultyLevel());
                        plan.setDifficultyLevel(newLevel);
                        patient.setLevel(newLevel);
                        planRepository.save(plan);
                        patientRepository.save(patient);
                        log.info("Régression patient {} → {}", patientId, newLevel);
                    }
                });
    }
    // ── Système Expert — niveau initial ───────
    private Level determineLevel(Patient patient) {
        // Patients âgés → DEBUTANT obligatoire
        if (patient.getAge() >= 65) return Level.DEBUTANT;
        return patient.getLevel();
    }

    private Level upgradeLevel(Level current) {
        return switch (current) {
            case DEBUTANT      -> Level.INTERMEDIAIRE;
            case INTERMEDIAIRE -> Level.AVANCE;
            case AVANCE        -> Level.AVANCE;
        };
    }

    private Level downgradeLevel(Level current) {
        return switch (current) {
            case AVANCE        -> Level.INTERMEDIAIRE;
            case INTERMEDIAIRE -> Level.DEBUTANT;
            case DEBUTANT      -> Level.DEBUTANT;
        };
    }

    // ── Getters ───────────────────────────────
    public RehabPlanResponse getActivePlan(UUID patientId) {
        return planMapper.toResponse(
                planRepository.findByPatientIdAndStatus(patientId, Status.ACTIVE)
                        .orElseThrow(() -> new BusinessException("Aucun plan actif")));
    }

    public List<RehabPlanResponse> getAllPlans(UUID patientId) {
        return planMapper.toResponseList(
                planRepository.findByPatientIdOrderByStartDateDesc(patientId));
    }
}
