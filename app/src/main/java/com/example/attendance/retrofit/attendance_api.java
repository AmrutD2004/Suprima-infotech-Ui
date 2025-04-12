package com.example.attendance.retrofit;


import com.example.attendance.model.LoginRequest;
import com.example.attendance.model.RegisterRequest;
import com.example.attendance.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface attendance_api {
    @POST("api/register")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    /**
     * Authenticate an existing user
     * @param loginRequest User login credentials
     * @return Call object with User response
     */
    @POST("api/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);
}