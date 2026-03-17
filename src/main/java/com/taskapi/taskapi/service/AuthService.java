package com.taskapi.taskapi.service;

import com.taskapi.taskapi.dto.auth.AuthResponse;
import com.taskapi.taskapi.dto.auth.LoginRequest;
import com.taskapi.taskapi.dto.auth.RegisterRequest;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.entity.enumeration.Role;
import com.taskapi.taskapi.exception.DuplicateResourceException;
import com.taskapi.taskapi.repository.UserRepository;
import com.taskapi.taskapi.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username already used");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already used");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .build();


        userRepository.save(user);

        return buildAuthResponse(request.getUsername(), request.getPassword());
    }

    public AuthResponse login(LoginRequest request){
        return buildAuthResponse(request.getUsername(), request.getPassword());
    }

    private AuthResponse buildAuthResponse(String username, String password){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        String token = jwtTokenProvider.generateToken(authentication);

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(token)
                .username(authentication.getName())
                .roles(roles)
                .build();
    }
}
