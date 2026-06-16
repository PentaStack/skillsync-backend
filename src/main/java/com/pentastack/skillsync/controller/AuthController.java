package com.pentastack.skillsync.controller;

import com.pentastack.skillsync.dto.AuthResponse;
import com.pentastack.skillsync.dto.LoginRequest;
import com.pentastack.skillsync.dto.RegisterRequest;
import com.pentastack.skillsync.dto.UserResponse;
import com.pentastack.skillsync.exception.ApiException;
import com.pentastack.skillsync.model.MentorProfile;
import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.model.StudentProfile;
import com.pentastack.skillsync.model.User;
import com.pentastack.skillsync.model.repository.MentorProfileRepository;
import com.pentastack.skillsync.model.repository.StudentProfileRepository;
import com.pentastack.skillsync.model.repository.UserRepository;
import com.pentastack.skillsync.security.JwtTokenProvider;
import com.pentastack.skillsync.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @org.springframework.beans.factory.annotation.Qualifier("modelUserRepository")
    private final UserRepository userRepository;
    @org.springframework.beans.factory.annotation.Qualifier("modelStudentProfileRepository")
    private final StudentProfileRepository studentProfileRepository;
    @org.springframework.beans.factory.annotation.Qualifier("modelMentorProfileRepository")
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * POST /api/auth/register -> Signup student or mentor
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email address is already in use");
        }

        // 1. Create and save the credential record
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();
        
        User savedUser = userRepository.save(user);

        // 2. Create the respective profile based on selected role
        if (registerRequest.getRole() == Role.STUDENT) {
            StudentProfile studentProfile = StudentProfile.builder()
                    .name(registerRequest.getName())
                    .user(savedUser)
                    .build();
            studentProfileRepository.save(studentProfile);
            savedUser.setStudentProfile(studentProfile);
        } else if (registerRequest.getRole() == Role.MENTOR) {
            MentorProfile mentorProfile = MentorProfile.builder()
                    .name(registerRequest.getName())
                    .user(savedUser)
                    .title(registerRequest.getTitle())
                    .hourlyRate(registerRequest.getHourlyRate())
                    .bio(registerRequest.getBio())
                    .build();
            mentorProfileRepository.save(mentorProfile);
            savedUser.setMentorProfile(mentorProfile);
        }

        // 3. Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserResponse userResponse = UserResponse.fromUser(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(userResponse, jwt));
    }

    /**
     * POST /api/auth/login -> Sign in credentials
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        UserResponse userResponse = UserResponse.fromUser(user);
        return ResponseEntity.ok(new AuthResponse(userResponse, jwt));
    }

    /**
     * GET /api/auth/profile -> Fetch current user metadata
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
}
