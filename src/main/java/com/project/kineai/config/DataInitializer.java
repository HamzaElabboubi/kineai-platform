package com.project.kineai.config;

import com.project.kineai.model.entity.User;
import com.project.kineai.model.enums.Role;
import com.project.kineai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createAdminIfNotExists();
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
}