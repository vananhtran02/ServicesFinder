package edu.sjsu.android.servicesfinder.model;

public class ServiceReview {

    private String id;
    private String serviceId;
    private String userName;
    private double rating;
    private String comment;
    private long timestamp;

    // Required empty constructor for Firestore
    public ServiceReview() {
    }

    public ServiceReview(String id,
                         String serviceId,
                         String userName,
                         double rating,
                         String comment,
                         long timestamp) {
        this.id = id;
        this.serviceId = serviceId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
