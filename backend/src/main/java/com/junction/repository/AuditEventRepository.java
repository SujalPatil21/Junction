package com.junction.repository;

import com.junction.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, String> {
    List<AuditEventEntity> findByTargetEntityIdOrderByTimestampDesc(String targetEntityId);
    List<AuditEventEntity> findByCategoryOrderByTimestampDesc(String category);
    List<AuditEventEntity> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);
    List<AuditEventEntity> findTop100ByOrderByTimestampDesc();
}

