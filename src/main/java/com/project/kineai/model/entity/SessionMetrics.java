package com.project.kineai.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "session_metrics")
@Builder
public class SessionMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    //------- Session Relation---------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
    //--------------------------------

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "joint_angles", nullable = false, columnDefinition = "TEXT")
    private String jointAngles;

    @Column(name = "conformity_pct", nullable = false, columnDefinition = "DECIMAL(5,2) CHECK (conformity_pct >= 0 AND conformity_pct <= 100)")
    private BigDecimal conformityPct;

    @Column(name = "reps_at_moment", nullable = false)
    @Builder.Default
    private Integer repsAtMoment = 0;


}
