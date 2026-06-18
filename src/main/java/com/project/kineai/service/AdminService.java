package com.project.kineai.service;

import com.project.kineai.dto.response.AdminStatsResponse;
import com.project.kineai.dto.response.KineResponse;
import com.project.kineai.dto.response.PatientResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.mapper.KineMapper;
import com.project.kineai.mapper.PatientMapper;
import com.project.kineai.model.entity.Kinesitherapeute;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.repository.KineRepository;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final PatientRepository patientRepository;
    private final KineRepository kineRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;
    private final KineMapper kineMapper;

    // ── Statistiques globales ─────────────────
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalPatients(patientRepository.count())
                .totalKines(kineRepository.count())
                .validatedKines(
                        kineRepository.countByValidatedTrue())
                .pendingKines(
                        kineRepository.countByValidatedFalse())
                .build();
    }

    // ── Liste TOUS les patients (vue admin) ───
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    // ── Liste TOUS les kinés (vue admin) ──────
    @Transactional(readOnly = true)
    public List<KineResponse> getAllKines() {
        List<Kinesitherapeute> kines = kineRepository.findAll();
        return kines.stream()
                .map(kine -> {
                    KineResponse response =
                            kineMapper.toResponse(kine);
                    response.setPatientCount(
                            kine.getPatients() != null
                                    ? kine.getPatients().size()
                                    : 0);
                    return response;
                })
                .toList();
    }

    // ── Supprimer définitivement un kiné ──────
    @Transactional
    public void deleteKine(UUID kineId) {
        Kinesitherapeute kine = kineRepository
                .findById(kineId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Kinésithérapeute introuvable"));

        if (kine.getPatients() != null
                && !kine.getPatients().isEmpty()) {
            throw new BusinessException(
                    "Impossible de supprimer un kiné ayant"
                            + " des patients assignés. Réassignez"
                            + " d'abord ses patients.");
        }

        userRepository.delete(kine.getUser());
        log.info("Kiné supprimé définitivement : {}",
                kine.getFullName());
    }

    // ── Réactiver un patient archivé ──────────
    @Transactional
    public PatientResponse reactivatePatient(
            UUID patientId) {
        Patient patient = patientRepository
                .findById(patientId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Patient introuvable"));

        patient.getUser().setActive(true);
        userRepository.save(patient.getUser());
        log.info("Patient réactivé : {}",
                patient.getFullName());
        return patientMapper.toResponse(patient);
    }
}