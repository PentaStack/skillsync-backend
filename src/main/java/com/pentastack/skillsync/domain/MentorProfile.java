package com.pentastack.skillsync.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class MentorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id")
    private Stack stack;

    private String name;
    private String title;
    private String bio;
    private boolean verified;
    private Double averageRating;
    private BigDecimal hourlyRate;

    protected MentorProfile() {}

    public MentorProfile(User user, Stack stack, String name, String title, String bio, boolean verified, Double averageRating, BigDecimal hourlyRate) {
        this.user = user;
        this.stack = stack;
        this.name = name;
        this.title = title;
        this.bio = bio;
        this.verified = verified;
        this.averageRating = averageRating;
        this.hourlyRate = hourlyRate;
    }

}
