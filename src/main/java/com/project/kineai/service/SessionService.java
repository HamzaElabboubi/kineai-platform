package com.project.kineai.service;

import com.project.kineai.dto.request.*;
import com.project.kineai.dto.response.SessionResponse;

import com.project.kineai.mapper.*;
import com.project.kineai.model.entity.*;
import com.project.kineai.model.enums.SessionStatus;
import com.project.kineai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMetricsRepository metricsRepository;
    private final ExerciseRepository exerciseRepository;
    private final RehabPlanRepository planRepository;
    private final SessionMapper sessionMapper;
    private final SessionMetricsMapper metricsMapper;
    private final PatientService patientService;
    private final com.project.kineai.service.BadgeService badgeService;
    private final RehabPlanService rehabPlanService;

    // ── Démarrer séance ───────────────────────
    @Transactional
    public SessionResponse startSession(CreateSessionRequest request) {
        Patient patient = patientService.getCurrentPatient();

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new BusinessException("Exercice introuvable"));

        RehabPlan plan = request.getPlanId() != null
                ? planRepository.findById(request.getPlanId()).orElse(null)
                : null;

        Session session = Session.builder()
                .patient(patient)
                .exercise(exercise)
                .rehabPlan(plan)
                .startTime(LocalDateTime.now())
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .build();

        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    // ── Sauvegarder métriques (toutes les 5s) ─
    @Transactional
    public void saveMetrics(UUID sessionId, SaveMetricsRequest request) {
        Session session = getSessionOrThrow(sessionId);

        SessionMetrics metrics = metricsMapper.toEntity(request);
        metrics.setSession(session);

        metricsRepository.save(metrics);
    }

    // ── Terminer séance ───────────────────────
    @Transactional
    public SessionResponse completeSession(UUID sessionId,
                                           CompleteSessionRequest request) {
        Session session = getSessionOrThrow(sessionId);
        Patient patient = session.getPatient();

        // Mettre à jour la séance
        session.setSessionStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setScore(request.getFinalScore());
        session.setRepsCompleted(request.getRepsCompleted());
        session.setJointAngles(request.getJointAngles());

        // Calculer XP — RG-29
        int xp = calculateXp(session);
        session.setXpEarned(xp);

        // Mise à jour XP et streak patient — RG-31
        patient.setTotalXp(patient.getTotalXp() + xp);
        patient.setStreakCount(patient.getStreakCount() + 1);

        session = sessionRepository.save(session);

        // Vérifier badges — RG-30
        badgeService.checkAndUnlockBadges(patient, session);

        // Vérifier progression/régression — RG-19/RG-20
        rehabPlanService.checkProgression(patient.getId());

        log.info("Séance {} terminée — score: {}", sessionId, request.getFinalScore());
        return sessionMapper.toResponse(session);
    }

    // ── Interrompre séance ────────────────────
    @Transactional
    public SessionResponse interruptSession(UUID sessionId) {
        Session session = getSessionOrThrow(sessionId);
        session.setSessionStatus(SessionStatus.INTERRUPTED);
        session.setEndTime(LocalDateTime.now());
        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    // ── Historique ────────────────────────────
    public List<SessionResponse> getMySessionHistory() {
        Patient patient = patientService.getCurrentPatient();
        return sessionMapper.toResponseList(
                sessionRepository.findByPatientIdOrderByStartTimeDesc(patient.getId()));
    }

    public List<SessionResponse> getSessionsByPatientId(UUID patientId) {
        return sessionMapper.toResponseList(
                sessionRepository.findByPatientIdOrderByStartTimeDesc(patientId));
    }

    // ── XP — RG-29 ───────────────────────────
    private int calculateXp(Session session) {
        int xp = 10; // base
        if (session.getScore() != null
                && session.getScore().doubleValue() > 80.0) {
            xp += 20; // bonus score > 80%
        }
        return xp;
    }

    private Session getSessionOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Séance introuvable"));
    }
}