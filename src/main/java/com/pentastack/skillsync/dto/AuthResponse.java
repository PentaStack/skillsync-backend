package com.pentastack.skillsync.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UserResponse user;
    private String token;
}
