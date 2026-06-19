package com.project.kineai.service;

import com.project.kineai.dto.response.ExerciseResponse;
import com.project.kineai.mapper.ExerciseMapper;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.repository.ExerciseRepository;
import com.project.kineai.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final PatientRepository patientRepository;
    private final ExerciseMapper exerciseMapper;

    // ── Tous les exercices (admin/kiné) ───────
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    // ── Exercices adaptés au patient connecté ─
    // Filtre automatique par pathologie + niveau
    // C'est le SYSTÈME EXPERT qui filtre, pas le kiné
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getMyExercises() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Patient patient = patientRepository
                .findByUser_Email(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient introuvable"));

        // Filtre automatique — pathologie ET niveau
        return exerciseRepository
                .findByBodyZoneAndDifficultyLevel(
                        patient.getPathology(),
                        patient.getLevel())
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    // ── Exercices par zone corporelle ─────────
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getByBodyZone(
            BodyZone bodyZone) {
        return exerciseRepository
                .findByBodyZone(bodyZone)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }
}