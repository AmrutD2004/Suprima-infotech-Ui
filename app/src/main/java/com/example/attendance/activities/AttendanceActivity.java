package com.example.attendance.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.attendance.R;
import com.example.attendance.model.ApiResponse;
import com.example.attendance.model.AttendanceRequest;
import com.example.attendance.model.User;
import com.example.attendance.retrofit.ApiService;
import com.example.attendance.retrofit.RetrofitService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_LOCATION_PERMISSION = 102;

    private ImageView ivPhoto;
    private Button btnCapture, btnCheckIn, btnCheckOut;
    private TextView tvDateTime, tvStatus;
    private ProgressBar progressBar;

    private ApiService apiService;
    private User currentUser;
    private Bitmap capturedImage;
    private FusedLocationProviderClient fusedLocationClient;
    private Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        // Initialize views
        ivPhoto = findViewById(R.id.ivPhoto);
        btnCapture = findViewById(R.id.btnCapture);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Retrofit service
        apiService = RetrofitService.getInstance().createService(ApiService.class);

        // Get current user from intent
        if (getIntent().hasExtra("user")) {
            currentUser = (User) getIntent().getSerializableExtra("user");
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Update current date and time
        updateDateTime();

        // Set up button click listeners
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });

        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAttendance("CHECK_IN");
            }
        });

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAttendance("CHECK_OUT");
            }
        });

        // Initially disable check-in/check-out buttons until photo is captured
        btnCheckIn.setEnabled(false);
        btnCheckOut.setEnabled(false);

        // Request location permission
        requestLocationPermission();
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        tvDateTime.setText(currentDateTime);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Toast.makeText(AttendanceActivity.this,
                                        "Location acquired: " + latitude + ", " + longitude,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AttendanceActivity.this,
                                        "Unable to get location",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            ivPhoto.setImageBitmap(capturedImage);

            // Enable check-in/check-out buttons
            btnCheckIn.setEnabled(true);
            btnCheckOut.setEnabled(true);
            tvStatus.setText("Photo captured. Ready to check in/out");
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void markAttendance(String type) {
        if (capturedImage == null) {
            Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Location not available. Please try again.", Toast.LENGTH_SHORT).show();
            requestLocationPermission();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnCheckIn.setEnabled(false);
        btnCheckOut.setEnabled(false);
        btnCapture.setEnabled(false);
        tvStatus.setText("Processing " + type.replace("_", " ") + "...");

        // Convert bitmap to base64 string
        String base64Image = convertBitmapToBase64(capturedImage);

        // Create attendance request
        AttendanceRequest request = new AttendanceRequest(
                currentUser.getId(),
                type,
                base64Image,
                latitude,
                longitude
        );

        // Make API call
        apiService.recordAttendance(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnCheckIn.setEnabled(true);
                btnCheckOut.setEnabled(true);
                btnCapture.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        String successMessage = type.equals("CHECK_IN") ?
                                "Checked In Successfully" : "Checked Out Successfully";
                        tvStatus.setText(successMessage);
                        Toast.makeText(AttendanceActivity.this,
                                apiResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Clear the image after successful submission
                        capturedImage = null;
                        ivPhoto.setImageResource(android.R.color.transparent);
                        btnCheckIn.setEnabled(false);
                        btnCheckOut.setEnabled(false);
                    } else {
                        tvStatus.setText("Failed: " + apiResponse.getMessage());
                        Toast.makeText(AttendanceActivity.this,
                                apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvStatus.setText("Server Error");
                    Toast.makeText(AttendanceActivity.this,
                            "Failed to process attendance", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCheckIn.setEnabled(true);
                btnCheckOut.setEnabled(true);
                btnCapture.setEnabled(true);
                tvStatus.setText("Network Error");
                Toast.makeText(AttendanceActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (capturedImage != null && !capturedImage.isRecycled()) {
            capturedImage.recycle();
            capturedImage = null;
        }
    }
}