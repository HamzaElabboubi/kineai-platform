package Model.Entity;


import Model.Enums.BodyZone;
import Model.Enums.Level;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "exercises")
@Builder
public class Exercise {

    @Id
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    //------- PlanExercise Relation--------
    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanExercise> planExercises;
    //-----------------------------


    //------- Basic info--------

    @Column(name = "name", nullable = false, length = 100,unique = true)
    private String name;

    @Column(name = "description", length = 500,nullable = false)
    private String description;

    //------- Exercise target --------
    @Enumerated(EnumType.STRING)
    @Column(name = "body_zone",nullable = false)
    private BodyZone bodyZone;

    @Column(name = "target_angle", nullable = false,columnDefinition = "INTEGER CHECK (target_angle >= 0 AND target_angle <= 180)")
    private Integer targetAngle;

    @Column(
            name = "tolerance_degree",
            nullable = false,
            columnDefinition = "INTEGER CHECK (tolerance_degree >= 0 AND tolerance_degree <= 45)"
    )
    @Builder.Default
    private Integer toleranceDegree = 15;

    //------- SESSION CONFIG --------
    @Column(name = "recommended_duration", nullable = false,columnDefinition = "INTEGER CHECK (recommended_duration > 0)")
    private Integer recommendedDuration; // Durée recommandée en secondes

    @Column(name = "reps_target", nullable = false,columnDefinition = "INTEGER CHECK (reps_target > 0)")
    private Integer repsTarget; // Nombre de répétitions ciblé par séance

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level",nullable = false)
    private Level difficultyLevel;

    //------- Mediapipe config --------
    @Column(name = "mediapipe_joints", nullable = false, length = 255)
    private String mediapipeJoints; // Liste des joints utilisés par MediaPipe, séparés par des virgules

}
