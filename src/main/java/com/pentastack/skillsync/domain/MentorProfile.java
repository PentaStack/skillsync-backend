package com.pentastack.skillsync.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity(name = "DomainMentorProfile")
@Table(name = "domain_mentor_profiles")
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private com.pentastack.skillsync.model.User user;

    @ManyToOne
    private Stack stack;

    private String displayName;
    private String title;
    private String bio;
    private boolean available;
    private Double rating;
    private BigDecimal hourlyRate;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    protected MentorProfile() {}

    public MentorProfile(com.pentastack.skillsync.model.User user, Stack stack, String displayName, String title, String bio, boolean available, Double rating, BigDecimal hourlyRate) {
        this.user = user;
        this.stack = stack;
        this.displayName = displayName;
        this.title = title;
        this.bio = bio;
        this.available = available;
        this.rating = rating;
        this.hourlyRate = hourlyRate;
    }

    public Long getId() { return id; }
    public com.pentastack.skillsync.model.User getUser() { return user; }
    public Stack getStack() { return stack; }
    public String getDisplayName() { return displayName; }
    public String getName() { return displayName; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
