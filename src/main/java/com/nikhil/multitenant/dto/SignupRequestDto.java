package com.nikhil.multitenant.dto;

import com.nikhil.multitenant.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SignupRequestDto {
    @Email
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private Role role;

    @NotNull
    private UUID tenantId;
}
