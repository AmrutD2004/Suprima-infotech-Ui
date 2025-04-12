package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.activities.AttendanceActivity;
import com.example.attendance.databinding.ActivityLoginBinding;
import com.example.attendance.model.ApiResponse;
import com.example.attendance.model.LoginRequest;
import com.example.attendance.model.User;
import com.example.attendance.retrofit.ApiService;
import com.example.attendance.retrofit.RetrofitService;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Retrofit service
        apiService = RetrofitService.getInstance().createService(ApiService.class);

        binding.loginButton.setOnClickListener(view -> attemptLogin());

        binding.registerButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (validateInputs(email, password)) {
            loginUser(email, password);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("All fields are required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email");
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Show loading state
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);

        apiService.loginUser(loginRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Hide loading state
                binding.progressBar.setVisibility(View.GONE);
                binding.loginButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Handle successful login
                        Gson gson = new Gson();
                        User user = gson.fromJson(gson.toJson(apiResponse.getData()), User.class); // Cast the data to User
                        if (user != null) {
                            handleSuccessfulLogin(user);
                        } else {
                            showToast("Invalid user data received");
                        }
                    } else {
                        handleLoginError(apiResponse.getMessage());
                    }
                } else {
                    handleLoginError(response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Hide loading state
                binding.progressBar.setVisibility(View.GONE);
                binding.loginButton.setEnabled(true);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void handleSuccessfulLogin(User user) {
        showToast("Login successful");
        // Store user session if needed
        Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
        intent.putExtra("user", user); // Pass user data to MainActivity
        startActivity(intent);
        finish(); // Close login activity to prevent going back
    }

    private void handleLoginError(String message) {
        showToast("Login failed: " + message);
    }

    private void handleLoginError(int statusCode) {
        String errorMessage = "Login failed";
        if (statusCode == 401) {
            errorMessage = "Invalid credentials";
        } else if (statusCode == 404) {
            errorMessage = "User not found";
        } else if (statusCode >= 500) {
            errorMessage = "Server error";
        }
        showToast(errorMessage + " (Code: " + statusCode + ")");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}