package com.pentastack.skillsync.domain;

import jakarta.persistence.*;

@Entity(name = "DomainStudentProfile")
@Table(name = "domain_student_profiles")
public class StudentProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	private User user;

	private String displayName;

	protected StudentProfile() {}

	public StudentProfile(User user, String displayName) {
		this.user = user;
		this.displayName = displayName;
	}

	public Long getId() { return id; }
	public User getUser() { return user; }
	public String getDisplayName() { return displayName; }
	public String getName() { return displayName; }
}
