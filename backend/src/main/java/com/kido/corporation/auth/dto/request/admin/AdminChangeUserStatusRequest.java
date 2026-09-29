package com.kido.corporation.auth.dto.request.admin;

import com.kido.corporation.auth.constant.EntityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminChangeUserStatusRequest {
    @NotNull
    EntityStatus status;

    String reason;
}
