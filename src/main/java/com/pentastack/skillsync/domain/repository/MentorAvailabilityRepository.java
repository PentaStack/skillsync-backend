package com.pentastack.skillsync.domain.repository;

import com.pentastack.skillsync.domain.MentorAvailability;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {
    List<MentorAvailability> findByMentor_Id(Long mentorId);
    List<MentorAvailability> findByMentor_IdAndDayOfWeek(Long mentorId, DayOfWeek dayOfWeek);
}
