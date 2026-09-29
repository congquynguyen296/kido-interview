package com.kido.corporation.auth.service;

import com.kido.corporation.auth.dto.request.user.ChangePasswordRequest;
import com.kido.corporation.auth.dto.request.user.DeactivateAccountRequest;
import com.kido.corporation.auth.dto.request.user.UpdateProfileRequest;
import com.kido.corporation.auth.dto.response.user.UserProfileResponse;

public interface UserService {

    UserProfileResponse getMe();

    UserProfileResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void deactivateAccount(DeactivateAccountRequest request);
}
