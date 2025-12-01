package edu.sjsu.android.servicesfinder.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    private String id;              // Firebase UID
    private String fullName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private List<String> favoriteProviders; // List of provider IDs
    private long createdAt;

    public Customer() {
        this.favoriteProviders = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Customer(String id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.favoriteProviders = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getFavoriteProviders() {
        return favoriteProviders;
    }

    public void setFavoriteProviders(List<String> favoriteProviders) {
        this.favoriteProviders = favoriteProviders;
    }

    public void addFavoriteProvider(String providerId) {
        if (this.favoriteProviders == null) {
            this.favoriteProviders = new ArrayList<>();
        }
        if (!this.favoriteProviders.contains(providerId)) {
            this.favoriteProviders.add(providerId);
        }
    }

    public void removeFavoriteProvider(String providerId) {
        if (this.favoriteProviders != null) {
            this.favoriteProviders.remove(providerId);
        }
    }

    public boolean isFavorite(String providerId) {
        return this.favoriteProviders != null && this.favoriteProviders.contains(providerId);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
