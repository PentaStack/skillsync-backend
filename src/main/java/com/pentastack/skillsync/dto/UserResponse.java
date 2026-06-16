package com.pentastack.skillsync.dto;

import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.model.User;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String avatar;
    private LocalDateTime createdAt;

    /**
     * Map a User domain entity to the UserResponse DTO.
     */
    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }

        String resolvedName = "";
        if (user.getRole() == Role.STUDENT && user.getStudentProfile() != null) {
            resolvedName = user.getStudentProfile().getName();
        } else if (user.getRole() == Role.MENTOR && user.getMentorProfile() != null) {
            resolvedName = user.getMentorProfile().getName();
        } else {
            resolvedName = "Admin User";
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(resolvedName)
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
