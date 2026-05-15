package com.project.kineai.Repository;

import com.project.kineai.model.entity.Session;
import com.project.kineai.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByPatientIdOrderByStartTimeDesc(UUID patientId);
    Optional<Session> findByPatientIdAndStatus(UUID patientId, SessionStatus status);
    @Query("SELECT s FROM Session s WHERE s.patient.id = :patientId ORDER BY s.startTime DESC LIMIT 1")
    Optional<Session> findLastSessionByPatientId(UUID patientId);
    List<Session> findByPatientIdAndStartTimeAfterAndStatus(UUID patientId, LocalDateTime date, SessionStatus status);
    @Query("SELECT AVG(s.score) FROM Session s WHERE s.patient.id = :patientId AND s.status = 'COMPLETED' ORDER BY s.startTime DESC LIMIT 3")
    Double getAverageScoreLastThreeSessions(UUID patientId);
}
