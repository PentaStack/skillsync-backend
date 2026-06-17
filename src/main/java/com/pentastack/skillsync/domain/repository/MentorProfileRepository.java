package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.MentorProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long>, JpaSpecificationExecutor<MentorProfile> {

    @EntityGraph(attributePaths = {"user", "stack"})
    Optional<MentorProfile> findWithUserById(Long id);

    Optional<MentorProfile> findByUser_Email(String email);

    @EntityGraph(attributePaths = {"user", "stack"})
    Page<MentorProfile> findByAvailable(boolean available, Pageable pageable);

    @Query(value = "SELECT m FROM DomainMentorProfile m JOIN FETCH m.user LEFT JOIN FETCH m.stack",
           countQuery = "SELECT COUNT(m) FROM DomainMentorProfile m")
    Page<MentorProfile> findAllWithDetails(Pageable pageable);

    long countByAvailable(boolean available);

    @Query("SELECT AVG(m.rating) FROM DomainMentorProfile m WHERE m.available = :available")
    Optional<Double> findAverageRatingByAvailable(@Param("available") boolean available);
}
