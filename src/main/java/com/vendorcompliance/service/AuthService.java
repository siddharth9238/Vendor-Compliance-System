package com.vendorcompliance.service;

import java.util.Date;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendorcompliance.dto.AuthRequest;
import com.vendorcompliance.dto.AuthResponse;
import com.vendorcompliance.dto.RegisterRequest;
import com.vendorcompliance.dto.UserProfileResponse;
import com.vendorcompliance.entity.AppUser;
import com.vendorcompliance.entity.Role;
import com.vendorcompliance.exception.BadRequestException;
import com.vendorcompliance.repository.UserRepository;
import com.vendorcompliance.security.CustomUserDetailsService;
import com.vendorcompliance.security.JwtUtil;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse registerVendor(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setEmail(request.getEmail());
        appUser.setFullName(request.getFullName());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRoles(Set.of(Role.VENDOR));
        appUser.setEnabled(true);

        userRepository.save(appUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(appUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return buildAuthResponse(appUser, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AppUser appUser = userDetailsService.getDomainUser(request.getUsername());
        UserDetails userDetails = userDetailsService.loadUserByUsername(appUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        auditService.logLogin(appUser.getUsername());
        return buildAuthResponse(appUser, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String authorizationHeader) {
        String token = jwtUtil.resolveToken(authorizationHeader);
        if (token == null || !jwtUtil.isTokenValid(token)) {
            throw new BadRequestException("Invalid bearer token");
        }
        if (jwtUtil.extractExpiration(token).before(new Date())) {
            throw new BadRequestException("Token already expired");
        }

        String username = jwtUtil.extractUsername(token);
        AppUser appUser = userDetailsService.getDomainUser(username);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newToken = jwtUtil.generateToken(userDetails);
        return buildAuthResponse(appUser, newToken);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        AppUser appUser = userDetailsService.getDomainUser(username);
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(appUser.getId());
        response.setUsername(appUser.getUsername());
        response.setEmail(appUser.getEmail());
        response.setFullName(appUser.getFullName());
        response.setRoles(appUser.getRoles());
        return response;
    }

    private AuthResponse buildAuthResponse(AppUser appUser, String token) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setExpiresInMs(jwtUtil.getExpirationMs());
        response.setUsername(appUser.getUsername());
        response.setRoles(appUser.getRoles());
        return response;
    }
}
