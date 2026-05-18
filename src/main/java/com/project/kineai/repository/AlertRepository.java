package com.project.kineai.repository;

import com.project.kineai.model.entity.Alert;
import com.project.kineai.model.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    // Alertes non résolues d'un kiné — dashboard kiné
    List<Alert> findByKinesitherapeuteIdAndResolvedFalse(UUID kineId);

    // Alertes d'un patient
    List<Alert> findByPatientId(UUID patientId);

    // Compter alertes non résolues — badge compteur dashboard
    long countByKinesitherapeuteIdAndResolvedFalse(UUID kineId);

    // Vérifier si alerte déjà existante — éviter doublons
    boolean existsByPatientIdAndTypeAndResolvedFalse(
            UUID patientId, AlertType type);

}
