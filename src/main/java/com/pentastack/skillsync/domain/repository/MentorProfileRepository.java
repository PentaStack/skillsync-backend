package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.MentorProfile;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    @EntityGraph(attributePaths = {"user", "stack"})
    Optional<MentorProfile> findWithUserById(Long id);

    Optional<MentorProfile> findByUser_Email(String email);

    // ponytail: per-mentor pessimistic lock — serializes concurrent bookings for same mentor; upgrade to Postgres EXCLUDE gist if throughput demands
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MentorProfile> findWithLockById(Long id);
}
