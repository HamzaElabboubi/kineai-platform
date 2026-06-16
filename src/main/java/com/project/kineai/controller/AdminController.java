package com.project.kineai.controller;

import com.project.kineai.dto.response.KineResponse;
import com.project.kineai.service.KineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administration",
        description = "Endpoints réservés à l'administrateur")
public class AdminController {

    private final KineService kineService;

    // ── Liste kinés en attente de validation ──
    @GetMapping("/kine/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Liste des kinés en attente")
    public ResponseEntity<List<KineResponse>>
    getPendingKines() {
        return ResponseEntity.ok(
                kineService.getPendingKines());
    }

    // ── Valider un compte kiné ─────────────────
    @PutMapping("/kine/{id}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Valider un compte kiné")
    public ResponseEntity<KineResponse>
    validateKine(@PathVariable UUID id) {
        return ResponseEntity.ok(
                kineService.validateKine(id));
    }

    // ── Rejeter un compte kiné ─────────────────
    @PutMapping("/kine/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rejeter un compte kiné")
    public ResponseEntity<Void>
    rejectKine(@PathVariable UUID id) {
        kineService.rejectKine(id);
        return ResponseEntity.noContent().build();
    }
}