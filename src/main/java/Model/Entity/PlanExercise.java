package Model.Entity;

import Model.Enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "plan_exercises")
@Builder
public class PlanExercise {

    @Id
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    //------- RehabPlan Relation--------
    @ManyToOne
    @JoinColumn(name = "rehab_plan_id", nullable = false)
    private RehabPlan rehabPlan;
    //-----------------------------

    //------- Exercise Relation--------
    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    //-----------------------------

    //------- Plaining--------
    @Column(name = "week_number", nullable = false, columnDefinition = "INTEGER CHECK (week_number > 0 AND week_number <= 4)")
    private Integer weekNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    //----------------------------

    //------- Exercise Config--------
    @Column(
            name = "reps_prescribed",
            nullable = false,
            columnDefinition = "INTEGER CHECK (reps_prescribed > 0)"
    )
    private Integer repsPrescribed;

    @Builder.Default
    @Column(
            name = "order_in_session",
            nullable = false,
            columnDefinition = "INTEGER CHECK (order_in_session > 0)"
    )
    private Integer orderInSession = 1;
}
