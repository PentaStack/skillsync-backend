package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.StudentProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser_Email(String email);

    @Query(value = "SELECT s FROM DomainStudentProfile s JOIN FETCH s.user",
           countQuery = "SELECT COUNT(s) FROM DomainStudentProfile s")
    Page<StudentProfile> findAllWithUser(Pageable pageable);
}
