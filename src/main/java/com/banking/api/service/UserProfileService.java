package com.banking.api.service;

import com.banking.api.model.dto.request.ChangePasswordRequest;
import com.banking.api.model.dto.request.UpdateProfileRequest;
import com.banking.api.model.dto.response.UserResponse;

public interface UserProfileService {

    UserResponse getProfile(String userId);

    UserResponse updateProfile(String userId, UpdateProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);
}
