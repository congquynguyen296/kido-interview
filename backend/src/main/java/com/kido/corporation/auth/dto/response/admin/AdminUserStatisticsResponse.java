package com.kido.corporation.auth.dto.response.admin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserStatisticsResponse {
    long totalUsers;
    Map<String, Long> usersByStatus;
    Map<String, Long> usersByProvider;
    long newUsersLast7Days;
    long newUsersLast30Days;
}
