package com.junction.repository;

import com.junction.entity.ProfileEntity;
import com.junction.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByClerkUserId(String clerkUserId);
    Optional<ProfileEntity> findByEmail(String email);
    List<ProfileEntity> findByRole(UserRole role);
    boolean existsByClerkUserId(String clerkUserId);
}

