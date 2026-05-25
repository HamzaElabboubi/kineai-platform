package com.project.kineai.services;


import com.project.kineai.dto.response.ExerciseResponse;
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    public List<ExerciseResponse> getByBodyZone(BodyZone bodyZone) {
        return exerciseRepository.findByBodyZone(bodyZone)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }
}