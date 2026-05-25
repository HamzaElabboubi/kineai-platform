package com.project.kineai.controller;

import com.project.kineai.dto.response.ExerciseResponse;
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercices", description = "Bibliothèque des exercices MVP")
public class ExerciseController {

    private final ExerciseService exerciseService;

    // ── Liste tous les exercices ──────────────
    // Public — Angular en a besoin avant authentification
    @GetMapping
    @Operation(summary = "Liste tous les exercices disponibles")
    public ResponseEntity<List<ExerciseResponse>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    // ── Exercices par zone corporelle ─────────
    @GetMapping("/zone/{bodyZone}")
    @Operation(summary = "Exercices filtrés par zone corporelle",
            description = "bodyZone : GENOU, EPAULE, DOS, HANCHE, COUDE")
    public ResponseEntity<List<ExerciseResponse>> getByBodyZone(
            @PathVariable BodyZone bodyZone) {
        return ResponseEntity.ok(
                exerciseService.getByBodyZone(bodyZone));
    }
}