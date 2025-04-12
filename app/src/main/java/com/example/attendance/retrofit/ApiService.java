package com.example.attendance.retrofit;

import com.example.attendance.model.ApiResponse;
import com.example.attendance.model.AttendanceRequest;
import com.example.attendance.model.LoginRequest;
import com.example.attendance.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/register")
    Call<ApiResponse> registerUser(@Body RegisterRequest request);

    @POST("api/login")
    Call<ApiResponse> loginUser(@Body LoginRequest request);

    @POST("api/attendance/record")
    Call<ApiResponse> recordAttendance(@Body AttendanceRequest request);

    @GET("api/attendance/user/{userId}")
    Call<ApiResponse> getUserAttendance(@Path("userId") Long userId);

    @GET("api/attendance/user/{userId}/date/{date}")
    Call<ApiResponse> getUserAttendanceByDate(
            @Path("userId") Long userId,
            @Path("date") String date);
}