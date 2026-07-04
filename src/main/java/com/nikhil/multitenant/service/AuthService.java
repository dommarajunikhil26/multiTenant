package com.nikhil.multitenant.service;

import com.nikhil.multitenant.dto.LoginRequestDto;
import com.nikhil.multitenant.dto.LoginResponseDto;
import com.nikhil.multitenant.dto.SignupMapper;
import com.nikhil.multitenant.dto.SignupRequestDto;
import com.nikhil.multitenant.model.Tenant;
import com.nikhil.multitenant.model.User;
import com.nikhil.multitenant.repository.TenantRepository;
import com.nikhil.multitenant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupMapper signupMapper;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       SignupMapper signupMapper, TenantRepository tenantRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.signupMapper = signupMapper;
        this.tenantRepository = tenantRepository;
        this.jwtService = jwtService;
    }

    public ResponseEntity<String> register(SignupRequestDto signupRequestDto) {
        if(userRepository.findByEmail(signupRequestDto.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Email already exists");
        }
        Tenant tenant = tenantRepository.findById(signupRequestDto.getTenantId()).orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        User savedUser = signupMapper.toEntity(signupRequestDto);
        savedUser.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        savedUser.setTenant(tenant);
        userRepository.save(savedUser);
        return ResponseEntity.ok().body("User registered successfully");
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        return userRepository.findByEmail(loginRequestDto.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword()))
                .map(user -> {
                    String jwtToken = jwtService.generateToken(user.getEmail(), user.getRole(), user.getTenant().getId());
                    loginResponseDto.setToken(jwtToken);
                    return ResponseEntity.ok().body(loginResponseDto);
                })
                .orElseGet(() -> {
                    loginResponseDto.setToken("Invalid Credentials");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                });
    }
}
