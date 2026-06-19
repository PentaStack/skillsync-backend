package com.pentastack.skillsync.model.repository;

import com.pentastack.skillsync.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository("modelStudentProfileRepository")
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    java.util.Optional<StudentProfile> findByUser_Email(String email);

    @Query(value = "SELECT s FROM StudentProfile s JOIN FETCH s.user",
           countQuery = "SELECT COUNT(s) FROM StudentProfile s")
    Page<StudentProfile> findAllWithUser(Pageable pageable);
}

