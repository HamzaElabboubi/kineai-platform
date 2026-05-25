package com.project.kineai.controller;

import com.project.kineai.dto.response.AlertResponse;
import com.project.kineai.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertes", description = "Alertes automatiques kinésithérapeute")
public class AlertController {

    private final AlertService alertService;

    // ── Alertes non résolues du kiné ──────────
    @GetMapping("/kine")
    @PreAuthorize("hasRole('KINE')")
    @Operation(summary = "Alertes en attente du kiné connecté",
            description = "Alertes INACTIVITY et SCORE non résolues")
    public ResponseEntity<List<AlertResponse>> getMyAlerts() {
        return ResponseEntity.ok(alertService.getMyAlerts());
    }

    // ── Résoudre une alerte ───────────────────
    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("hasRole('KINE')")
    @Operation(summary = "Marquer une alerte comme résolue (RG-26)")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable UUID alertId) {
        return ResponseEntity.ok(alertService.resolveAlert(alertId));
    }

    // ── Compter alertes en attente ────────────
    @GetMapping("/kine/count")
    @PreAuthorize("hasRole('KINE')")
    @Operation(summary = "Nombre d'alertes non résolues",
            description = "Utilisé pour afficher le badge compteur dans le dashboard")
    public ResponseEntity<Long> countPendingAlerts() {
        return ResponseEntity.ok(alertService.countPendingAlerts());
    }
}