package com.pentastack.skillsync.model.repository;

import com.pentastack.skillsync.model.MentorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("modelMentorProfileRepository")
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<MentorProfile> findByIsVerified(boolean isVerified, Pageable pageable);

    long countByIsVerified(boolean isVerified);
}

