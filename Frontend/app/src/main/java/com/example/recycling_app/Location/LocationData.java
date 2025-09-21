package com.example.recycling_app.Location;

public class LocationData {
    public double latitude;
    public double longitude;
    public String address;
    public String type;

    public LocationData() {}

    public LocationData(double latitude, double longitude, String address, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

}
