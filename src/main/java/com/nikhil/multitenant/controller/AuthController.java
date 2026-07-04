package com.nikhil.multitenant.controller;

import com.nikhil.multitenant.dto.LoginRequestDto;
import com.nikhil.multitenant.dto.LoginResponseDto;
import com.nikhil.multitenant.dto.SignupRequestDto;
import com.nikhil.multitenant.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid SignupRequestDto signupRequestDto){
        return authService.register(signupRequestDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto){
        return authService.login(loginRequestDto);
    }


}
