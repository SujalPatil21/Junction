package com.junction.repository;

import com.junction.entity.OperationalInterventionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OperationalInterventionRepository extends JpaRepository<OperationalInterventionEntity, String> {
    List<OperationalInterventionEntity> findByTargetZoneId(String targetZoneId);
    List<OperationalInterventionEntity> findByStatus(String status);
    List<OperationalInterventionEntity> findByExpiresAtAfterAndStatus(Instant now, String status);
    List<OperationalInterventionEntity> findTop50ByOrderByProposedAtDesc();
}

