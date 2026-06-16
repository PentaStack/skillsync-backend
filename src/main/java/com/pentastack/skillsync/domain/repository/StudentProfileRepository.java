package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.StudentProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser_Email(String email);
}
