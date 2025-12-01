package edu.sjsu.android.servicesfinder.database;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sjsu.android.servicesfinder.R;
import edu.sjsu.android.servicesfinder.model.Customer;

/**
 * CustomerDatabase - Data Access Layer for Customer operations
 *
 * Handles all Firestore operations for customers:
 * - Read customer by UID or phone
 * - Create customer (using Firebase UID as document ID)
 * - Update customer
 * - Delete customer
 * - Manage favorite providers
 *
 * MVC ROLE: Database/Model layer
 */
public class CustomerDatabase {
    private static final String COLLECTION_CUSTOMERS = "customers";
    private final FirebaseFirestore db;
    private final Context context;

    public CustomerDatabase(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // =========================================================
    // READ CUSTOMER BY FIREBASE UID
    // =========================================================

    /**
     * Get customer by Firebase UID (document ID)
     */
    public void getCustomerById(String customerId, OnCustomerLoadedListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Customer customer = documentSnapshotToCustomer(doc);
                        listener.onSuccess(customer);
                    } else {
                        listener.onError(context.getString(R.string.error_customer_not_found));
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    // =========================================================
    // READ CUSTOMER BY PHONE (for Phone-based sign-in)
    // =========================================================
    public void getCustomerByPhone(String phone, OnCustomerLoadedListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Customer customer = query.getDocuments()
                                .get(0)
                                .toObject(Customer.class);
                        listener.onSuccess(customer);
                    } else {
                        listener.onError(context.getString(R.string.error_customer_not_found_with_phone));
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(context.getString(R.string.error_fetching_customer, e.getMessage()));
                });
    }

    // =========================================================
    // CREATE NEW CUSTOMER USING FIREBASE UID AS DOCUMENT ID
    // =========================================================
    public void addCustomer(Customer customer, OnCustomerOperationListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customer.getId())  // Use Firebase UID as document ID
                .set(customerToMap(customer))
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(context.getString(R.string.success_customer_saved));
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    // =========================================================
    // UPDATE CUSTOMER FIELDS
    // =========================================================
    public void updateCustomerFields(String customerId, Map<String, Object> updates, OnCustomerOperationListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(context.getString(R.string.success_customer_updated));
                })
                .addOnFailureListener(e -> {
                    listener.onError(context.getString(R.string.error_update_failed, e.getMessage()));
                });
    }

    // =========================================================
    // DELETE CUSTOMER
    // =========================================================
    public void deleteCustomer(String customerId, OnCustomerOperationListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(context.getString(R.string.success_customer_deleted));
                })
                .addOnFailureListener(e -> {
                    listener.onError(context.getString(R.string.error_delete_customer_failed, e.getMessage()));
                });
    }

    // =========================================================
    // FAVORITE PROVIDERS OPERATIONS
    // =========================================================

    /**
     * Add a provider to customer's favorites
     */
    public void addFavoriteProvider(String customerId, String providerId, OnCustomerOperationListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    Customer customer = documentSnapshotToCustomer(doc);
                    customer.addFavoriteProvider(providerId);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("favoriteProviders", customer.getFavoriteProviders());

                    updateCustomerFields(customerId, updates, listener);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Remove a provider from customer's favorites
     */
    public void removeFavoriteProvider(String customerId, String providerId, OnCustomerOperationListener listener) {
        db.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    Customer customer = documentSnapshotToCustomer(doc);
                    customer.removeFavoriteProvider(providerId);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("favoriteProviders", customer.getFavoriteProviders());

                    updateCustomerFields(customerId, updates, listener);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    // =========================================================
    // CONVERTER METHODS
    // =========================================================

    /**
     * Convert DocumentSnapshot(Firebase data) to Customer
     */
    private Customer documentSnapshotToCustomer(DocumentSnapshot doc) {
        Customer customer = new Customer();
        customer.setId(doc.getString("id"));
        customer.setFullName(doc.getString("fullName"));
        customer.setEmail(doc.getString("email"));
        customer.setPhone(doc.getString("phone"));
        customer.setProfileImageUrl(doc.getString("profileImageUrl"));

        // Handle favoriteProviders list
        List<String> favorites = (List<String>) doc.get("favoriteProviders");
        if (favorites != null) {
            customer.setFavoriteProviders(favorites);
        }

        // Handle createdAt
        Long createdAt = doc.getLong("createdAt");
        if (createdAt != null) {
            customer.setCreatedAt(createdAt);
        }

        return customer;
    }

    /**
     * Converts a Customer object into a Map<String, Object> for Firestore
     */
    private Map<String, Object> customerToMap(Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("fullName", customer.getFullName());
        map.put("email", customer.getEmail());
        map.put("phone", customer.getPhone());
        map.put("profileImageUrl", customer.getProfileImageUrl());
        map.put("favoriteProviders", customer.getFavoriteProviders());
        map.put("createdAt", customer.getCreatedAt());

        return map;
    }

    // =========================================================
    // CALLBACK INTERFACES
    // =========================================================

    public interface OnCustomerLoadedListener {
        void onSuccess(Customer customer);
        void onError(String errorMessage);
    }

    public interface OnCustomerOperationListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
}
