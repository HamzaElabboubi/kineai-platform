package com.project.kineai.Repository;

import com.project.kineai.model.entity.Kinesitherapeute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KineRepository extends JpaRepository<Kinesitherapeute, UUID> {
    Optional<Kinesitherapeute> findByUserId(UUID userId);
}
