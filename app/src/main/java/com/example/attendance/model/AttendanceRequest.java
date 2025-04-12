    package com.example.attendance.model;

    public class AttendanceRequest {
        private Long userId;
        private String type;
        private String base64Image;
        private Double latitude;
        private Double longitude;

        public AttendanceRequest(Long userId, String type, String base64Image, Double latitude, Double longitude) {
            this.userId = userId;
            this.type = type;
            this.base64Image = base64Image;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBase64Image() {
            return base64Image;
        }

        public void setBase64Image(String base64Image) {
            this.base64Image = base64Image;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }