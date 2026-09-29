package com.kido.corporation.auth.dto.request.admin;

import com.kido.corporation.auth.constant.AuthProvider;
import com.kido.corporation.auth.constant.EntityStatus;
import com.kido.corporation.auth.dto.common.PageRequestParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserSearchRequest extends PageRequestParam {
    String keyword; // match email, username, fullName, phoneNumber
    EntityStatus status;
    String role;
    AuthProvider provider;
    Boolean emailVerified;
    Instant createdFrom;
    Instant createdTo;
}
