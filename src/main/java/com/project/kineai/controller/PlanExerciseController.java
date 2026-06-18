package com.project.kineai.controller;

import com.project.kineai.dto.request.AssignExerciseRequest;
import com.project.kineai.dto.response.PlanExerciseResponse;
import com.project.kineai.service.PlanExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plan-exercises")
@RequiredArgsConstructor
@Tag(name = "Exercices du plan",
        description = "Affectation d'exercices aux plans de rééducation")
public class PlanExerciseController {

    private final PlanExerciseService planExerciseService;

    // ── Assigner un exercice ───────────────────
    @PostMapping
    @PreAuthorize("hasRole('KINE')")
    @Operation(summary = "Assigner un exercice au plan d'un patient")
    public ResponseEntity<PlanExerciseResponse> assign(
            @Valid @RequestBody AssignExerciseRequest request) {
        return ResponseEntity.ok(
                planExerciseService.assignExercise(request));
    }

    // ── Liste complète des exercices du plan ──
    @GetMapping("/plan/{planId}")
    @PreAuthorize("hasAnyRole('KINE', 'PATIENT')")
    @Operation(summary = "Liste des exercices d'un plan")
    public ResponseEntity<List<PlanExerciseResponse>>
    getPlanExercises(@PathVariable UUID planId) {
        return ResponseEntity.ok(
                planExerciseService.getPlanExercises(planId));
    }

    // ── Exercices de la semaine courante ──────
    @GetMapping("/plan/{planId}/week/{weekNumber}")
    @PreAuthorize("hasAnyRole('KINE', 'PATIENT')")
    @Operation(summary = "Exercices d'une semaine donnée du plan")
    public ResponseEntity<List<PlanExerciseResponse>>
    getWeekExercises(
            @PathVariable UUID planId,
            @PathVariable Integer weekNumber) {
        return ResponseEntity.ok(
                planExerciseService.getCurrentWeekExercises(
                        planId, weekNumber));
    }

    // ── Retirer un exercice du plan ───────────
    @DeleteMapping("/{planExerciseId}")
    @PreAuthorize("hasRole('KINE')")
    @Operation(summary = "Retirer un exercice du plan")
    public ResponseEntity<Void> remove(
            @PathVariable UUID planExerciseId) {
        planExerciseService.removeExercise(planExerciseId);
        return ResponseEntity.noContent().build();
    }
}