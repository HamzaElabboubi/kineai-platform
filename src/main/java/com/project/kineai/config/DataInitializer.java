package com.project.kineai.config;

import com.project.kineai.model.entity.Exercise;
import com.project.kineai.model.entity.User;
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Role;
import com.project.kineai.repository.ExerciseRepository;
import com.project.kineai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExerciseRepository exerciseRepository;

    @Override
    public void run(ApplicationArguments args) {
        createAdminIfNotExists();
        createExercisesIfNotExist();
    }

    private void createAdminIfNotExists() {
        // Vérifier si l'admin existe déjà
        if (userRepository.existsByEmail("admin@kineai.com")) {
            log.info("Admin déjà existant — aucune création");
            return;
        }

        // Créer le compte admin
        User admin = User.builder()
                .email("admin@kineai.com")
                .password(passwordEncoder.encode("Admin@2025"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("✅ Compte admin créé — admin@kineai.com");
    }

    private void createExercisesIfNotExist() {
        if (exerciseRepository.count() > 0) {
            log.info("Exercices déjà présents");
            return;
        }

        List<Exercise> exercises = List.of(
                Exercise.builder()
                        .name("Flexion genou")
                        .description("Fléchissez le genou jusqu'à 90°"
                                + " en position assise")
                        .bodyZone(BodyZone.GENOU)
                        .targetAngle(90)
                        .toleranceDegree(15)
                        .repsTarget(10)
                        .recommendedDuration(30)
                        .difficultyLevel(Level.DEBUTANT)
                        .mediapipeJoints(
                                "LEFT_HIP,LEFT_KNEE,LEFT_ANKLE")
                        .build(),

                Exercise.builder()
                        .name("Extension genou")
                        .description("Étendez le genou jusqu'à"
                                + " la position droite")
                        .bodyZone(BodyZone.GENOU)
                        .targetAngle(170)
                        .toleranceDegree(10)
                        .repsTarget(12)
                        .recommendedDuration(30)
                        .difficultyLevel(Level.INTERMEDIAIRE)
                        .mediapipeJoints(
                                "LEFT_HIP,LEFT_KNEE,LEFT_ANKLE")
                        .build(),

                Exercise.builder()
                        .name("Élévation épaule")
                        .description("Élevez le bras latéralement"
                                + " jusqu'à 90°")
                        .bodyZone(BodyZone.EPAULE)
                        .targetAngle(90)
                        .toleranceDegree(15)
                        .repsTarget(10)
                        .recommendedDuration(25)
                        .difficultyLevel(Level.DEBUTANT)
                        .mediapipeJoints(
                                "LEFT_SHOULDER,LEFT_ELBOW,LEFT_WRIST")
                        .build(),

                Exercise.builder()
                        .name("Rotation épaule")
                        .description("Effectuez une rotation"
                                + " externe de l'épaule à 45°")
                        .bodyZone(BodyZone.EPAULE)
                        .targetAngle(45)
                        .toleranceDegree(10)
                        .repsTarget(8)
                        .recommendedDuration(20)
                        .difficultyLevel(Level.INTERMEDIAIRE)
                        .mediapipeJoints(
                                "LEFT_SHOULDER,LEFT_ELBOW,LEFT_WRIST")
                        .build(),

                Exercise.builder()
                        .name("Extension dos")
                        .description("Redressez le dos en position"
                                + " debout jusqu'à 160°")
                        .bodyZone(BodyZone.DOS)
                        .targetAngle(160)
                        .toleranceDegree(15)
                        .repsTarget(10)
                        .recommendedDuration(30)
                        .difficultyLevel(Level.DEBUTANT)
                        .mediapipeJoints(
                                "LEFT_HIP,LEFT_SHOULDER,LEFT_EAR")
                        .build()
        );

        exerciseRepository.saveAll(exercises);
        log.info("✅ {} exercices MVP créés",
                exercises.size());
    }
}