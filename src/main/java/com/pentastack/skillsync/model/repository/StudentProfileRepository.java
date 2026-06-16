package com.pentastack.skillsync.model.repository;

import com.pentastack.skillsync.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("modelStudentProfileRepository")
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
}

