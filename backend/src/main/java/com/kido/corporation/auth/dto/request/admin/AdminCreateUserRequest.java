package com.kido.corporation.auth.dto.request.admin;

import com.kido.corporation.auth.constant.EntityStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateUserRequest {
    @NotBlank
    @Email
    String email;

    String password;

    String fullName;

    @NotNull
    List<String> roles;

    @NotNull
    EntityStatus status;

    Boolean emailVerified;
}
