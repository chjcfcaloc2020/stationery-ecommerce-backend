package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.AuthenticationRequest;
import com.stationery_ecommerce.dto.request.RegisterRequest;
import com.stationery_ecommerce.dto.response.AuthenticationResponse;
import com.stationery_ecommerce.entity.User;
import com.stationery_ecommerce.exception.payload.ResourceAlreadyExistsException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        var newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .role("CUSTOMER")
                .isActive(true)
                .build();

        var savedUser = userRepository.save(newUser);

        var springUser = new org.springframework.security.core.userdetails.User(
                savedUser.getEmail(),
                savedUser.getHashedPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole()))
        );
        var jwtToken = jwtService.generateToken(springUser);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Account was baned");
        }

        var springUser = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getHashedPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        var jwtToken = jwtService.generateToken(springUser);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
