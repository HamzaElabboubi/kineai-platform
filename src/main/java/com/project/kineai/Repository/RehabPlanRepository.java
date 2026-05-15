package com.project.kineai.Repository;

import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RehabPlanRepository extends JpaRepository<RehabPlan, UUID> {
    Optional<RehabPlan> findByPatientIdAndStatus(UUID patientId, Status status);
    List<RehabPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
