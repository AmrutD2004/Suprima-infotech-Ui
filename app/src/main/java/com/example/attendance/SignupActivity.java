package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.attendance.databinding.ActivitySignupBinding;
import com.example.attendance.model.ApiResponse;
import com.example.attendance.model.RegisterRequest;
import com.example.attendance.model.User;
import com.example.attendance.retrofit.ApiService;
import com.example.attendance.retrofit.RetrofitService;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Retrofit service
        apiService = RetrofitService.getInstance().createService(ApiService.class);

        binding.createAccountButton.setOnClickListener(view -> attemptRegistration());

        binding.goToLoginButton.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegistration() {
        String fullname = binding.fullNameEditText.getText().toString().trim();
        String empId = binding.employeeIdEditText.getText().toString().trim();
        String email = binding.registerEmailEditText.getText().toString().trim();
        String password = binding.registerPasswordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        if (validateInputs(fullname, empId, email, password, confirmPassword)) {
            registerUser(fullname, empId, email, password);
        }
    }

    private boolean validateInputs(String fullname, String empId, String email,
                                   String password, String confirmPassword) {
        if (fullname.isEmpty() || empId.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("All fields are required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords don't match");
            return false;
        }

        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void registerUser(String fullname, String empId, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(fullname, empId, email, password);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.createAccountButton.setEnabled(false);

        apiService.registerUser(registerRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.createAccountButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Gson gson = new Gson();
                        User user = gson.fromJson(gson.toJson(apiResponse.getData()), User.class); // Cast the data to User
                        if (user != null) {
                            handleSuccessfulRegistration(user);
                        } else {
                            showToast("Registration successful but user data not received");
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        handleRegistrationError(apiResponse.getMessage());
                    }
                } else {
                    handleRegistrationError(response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.createAccountButton.setEnabled(true);
                showToast("Network error: " + t.getLocalizedMessage());
            }
        });
    }

    private void handleSuccessfulRegistration(User user) {
        showToast("Registration successful");
        // You can directly login the user or redirect to login page
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.putExtra("email", user.getEmail()); // Optional: Pre-fill email in login
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(String message) {
        showToast("Registration failed: " + message);
    }

    private void handleRegistrationError(int statusCode) {
        String errorMessage = "Registration failed";
        if (statusCode == 409) {
            errorMessage = "User already exists";
        } else if (statusCode == 400) {
            errorMessage = "Invalid request data";
        } else if (statusCode >= 500) {
            errorMessage = "Server error";
        }
        showToast(errorMessage + " (Code: " + statusCode + ")");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}