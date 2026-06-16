package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.MentorProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    @EntityGraph(attributePaths = {"user", "stack"})
    Optional<MentorProfile> findWithUserById(Long id);

    Optional<MentorProfile> findByUser_Email(String email);
}
