package com.project.kineai.scheduler;

import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.enums.AlertType;
import com.project.kineai.model.enums.SessionStatus;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.SessionRepository;
import com.project.kineai.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final PatientRepository patientRepository;
    private final SessionRepository sessionRepository;
    private final AlertService alertService;

    // ── RG-23 : Alerte inactivité ─────────────
    // Vérification toutes les heures
    @Scheduled(cron = "0 0 * * * *")
    public void checkInactivity() {
        log.info("Scheduler — Vérification inactivité patients...");

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Patient> patients = patientRepository.findAll();

        patients.forEach(patient -> {
            // Chercher la dernière séance complétée
            sessionRepository
                    .findFirstByPatientIdOrderByStartTimeDesc(patient.getId())
                    .ifPresent(lastSession -> {
                        // Si dernière séance > 3 jours → alerte
                        if (lastSession.getStartTime().isBefore(threeDaysAgo)) {
                            alertService.createAlert(
                                    patient,
                                    patient.getKine(),
                                    AlertType.INACTIVITY,
                                    "Le patient " + patient.getFullName() +
                                            " est inactif depuis plus de 3 jours. " +
                                            "Dernière séance : " +
                                            lastSession.getStartTime().toLocalDate()
                            );
                            log.info("Alerte INACTIVITY créée pour : {}",
                                    patient.getFullName());
                        }
                    });
        });
    }

    // ── RG-24 : Alerte score insuffisant ──────
    // Vérification toutes les heures (décalée de 30 min)
    @Scheduled(cron = "0 30 * * * *")
    public void checkLowScores() {
        log.info("Scheduler — Vérification scores insuffisants...");

        List<Patient> patients = patientRepository.findAll();

        patients.forEach(patient -> {
            Double avgScore = sessionRepository
                    .getAverageScoreLastThreeSessions(patient.getId());

            // Si score moyen < 60% → alerte kiné
            if (avgScore != null && avgScore < 60.0) {
                alertService.createAlert(
                        patient,
                        patient.getKine(),
                        AlertType.SCORE,
                        "Le patient " + patient.getFullName() +
                                " a un score moyen de " +
                                String.format("%.1f", avgScore) +
                                "% sur ses 3 dernières séances. " +
                                "Une intervention est recommandée."
                );
                log.info("Alerte SCORE créée pour : {} — score: {}%",
                        patient.getFullName(),
                        String.format("%.1f", avgScore));
            }
        });
    }

    // ── RG-25 : Rappel patient à 18h ──────────
    @Scheduled(cron = "0 0 18 * * *")
    public void sendEveningReminders() {
        log.info("Scheduler — Envoi rappels patients 18h...");

        LocalDateTime todayStart = LocalDateTime.now()
                .toLocalDate()
                .atStartOfDay();

        List<Patient> patients = patientRepository.findAll();

        patients.forEach(patient -> {
            // Vérifier si séance effectuée aujourd'hui
            boolean hasDoneSessionToday = !sessionRepository
                    .findByPatientIdAndStartTimeAfterAndSessionStatus(
                            patient.getId(),
                            todayStart,
                            SessionStatus.COMPLETED
                    ).isEmpty();

            if (!hasDoneSessionToday) {
                // TODO Sprint 4 — intégrer Firebase Cloud Messaging
                log.info("Rappel 18h → patient : {}",
                        patient.getFullName());
            }
        });
    }

    // ── RG-22 : Clôture automatique plans expirés ─
    // Vérification chaque jour à minuit
    @Scheduled(cron = "0 0 0 * * *")
    public void closeExpiredPlans() {
        log.info("Scheduler — Clôture plans expirés...");
        // TODO — implémenter dans RehabPlanService
        // planRepository.findExpiredActivePlans(LocalDate.now())
        //     .forEach(plan -> {
        //         plan.setStatus(Status.DONE);
        //         planRepository.save(plan);
        //     });
    }

    // ── RG-32 : Réinitialisation streak à minuit ──
    @Scheduled(cron = "0 5 0 * * *")
    public void resetStreaks() {
        log.info("Scheduler — Vérification streaks...");

        LocalDateTime yesterdayStart = LocalDateTime.now()
                .minusDays(1)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime yesterdayEnd = LocalDateTime.now()
                .toLocalDate()
                .atStartOfDay();

        List<Patient> patients = patientRepository.findAll();

        patients.forEach(patient -> {
            // Si aucune séance hier → streak = 0
            boolean hadSessionYesterday = !sessionRepository
                    .findByPatientIdAndStartTimeAfterAndSessionStatus(
                            patient.getId(),
                            yesterdayStart,
                            SessionStatus.COMPLETED
                    ).isEmpty();

            if (!hadSessionYesterday && patient.getStreakCount() > 0) {
                patient.setStreakCount(0);
                patientRepository.save(patient);
                log.info("Streak réinitialisé pour : {}",
                        patient.getFullName());
            }
        });
    }
}