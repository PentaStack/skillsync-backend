package com.pentastack.skillsync.repository;

import com.pentastack.skillsync.model.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
}
