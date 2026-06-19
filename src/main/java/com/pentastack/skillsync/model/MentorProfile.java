package com.pentastack.skillsync.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mentor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false)
    private String name;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Column(name = "average_rating", nullable = false)
    @Builder.Default
    private double averageRating = 0.0;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private boolean available = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id", nullable = false)
    private com.pentastack.skillsync.domain.Stack stack;

    public String getDisplayName() {
        return this.name;
    }

    public void updateProfile(String title, String bio, BigDecimal hourlyRate, boolean available) {
        this.title = title;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.available = available;
    }
}
