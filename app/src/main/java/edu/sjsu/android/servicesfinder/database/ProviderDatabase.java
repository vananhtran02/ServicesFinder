package edu.sjsu.android.servicesfinder.database;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.sjsu.android.servicesfinder.model.Provider;

/**
 * ProviderDatabase - Data Access Layer for Provider operations
 *
 * Handles all Firestore operations for providers:
 * - Read provider by UID or phone
 * - Create provider (using Firebase UID as document ID)
 * - Update provider
 * - Delete provider
 * - Manage provider services subcollection
 *
 * MVC ROLE: Database/Model layer
 */
public class ProviderDatabase {
    private static final String TAG = "ProviderDatabase";
    private static final String COLLECTION_PROVIDERS = "providers";
    private final FirebaseFirestore db;
    public ProviderDatabase() {
        this.db = FirebaseFirestore.getInstance();
    }

    // =========================================================
    // READ PROVIDER BY FIREBASE UID
    // =========================================================

    /**
     * Get provider by Firebase UID (document ID)
     */
    public void getProviderById(String providerId, OnProviderLoadedListener listener) {
        db.collection(COLLECTION_PROVIDERS)
                .document(providerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Provider provider = documentSnapshotToProvider(doc);
                        listener.onSuccess(provider);
                    } else {
                        listener.onError("Provider not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching provider by ID", e);
                    listener.onError(e.getMessage());
                });
    }

    // =========================================================
    // READ PROVIDER BY PHONE (for Phone-based sign-in)
    // =========================================================
    public void getProviderByPhone(String phone, OnProviderLoadedListener listener) {
        db.collection(COLLECTION_PROVIDERS)
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Provider provider = query.getDocuments()
                                .get(0)
                                .toObject(Provider.class);
                        listener.onSuccess(provider);
                    } else {
                        listener.onError("No provider found with this phone");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching provider by phone", e);
                    listener.onError("Error fetching provider: " + e.getMessage());
                });
    }

    // =========================================================
    // CREATE NEW PROVIDER USING FIREBASE UID AS DOCUMENT ID
    // =========================================================
    public void addProvider(Provider provider, OnProviderOperationListener listener) {
        // Log the document path and payload before writing
        Log.d(TAG, "Saving to /providers/" + provider.getId());
        Log.d(TAG, "Payload: " + providerToMap(provider).toString());

        db.collection(COLLECTION_PROVIDERS)
                .document(provider.getId())  // Use Firebase UID as document ID
                .set(providerToMap(provider))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Provider saved: " + provider.getId());
                    listener.onSuccess("Provider saved");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding provider", e);
                    listener.onError(e.getMessage());
                });
    }

    // =========================================================
    // Convert DocumentSnapshot(Firebase data) to Provider
    // =========================================================
    private Provider documentSnapshotToProvider(DocumentSnapshot doc) {
        Provider provider = new Provider();
        provider.setId(doc.getString("id"));
        provider.setFullName(doc.getString("fullName"));
        provider.setEmail(doc.getString("email"));
        provider.setAddress(doc.getString("address"));
        provider.setPhone(doc.getString("phone"));
        provider.setPassword(doc.getString("password"));
        return provider;
    }

    //**********************************************************************************
    // Converts a Provider object into a Map<String, Object>, so it can be saved to Firestore
    //************************************************************************************
    private Map<String, Object> providerToMap(Provider provider) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", provider.getId());
        map.put("fullName", provider.getFullName());
        map.put("email", provider.getEmail());
        map.put("address", provider.getAddress());
        map.put("phone", provider.getPhone());

        // Use only for testing/demo purposes.
        map.put("password", provider.getPassword());

        return map;
    }

    // =========================================================
    // CALLBACK INTERFACES
    // =========================================================

    public interface OnProviderLoadedListener {
        void onSuccess(Provider provider);
        void onError(String errorMessage);
    }

    public interface OnProviderOperationListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    // =========================================================
    // UPDATE PROVIDER FIELDS (in setting)
    // =========================================================

    /**
     * Updates specific fields of a provider document in Firestore.
     * Accepts a UID and a map of fields to update.
     */
    public void updateProviderFields(String providerId, Map<String, Object> updates, OnProviderOperationListener listener) {
        db.collection(COLLECTION_PROVIDERS)
                .document(providerId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Provider fields updated: " + providerId);
                    listener.onSuccess("Provider updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating provider", e);
                    listener.onError("Update failed: " + e.getMessage());
                });
    }
    // =========================================================
    // Deletes a provider document from Firestore by UID.
    // =========================================================

    public void deleteProvider(String providerId, OnProviderOperationListener listener) {
        // Step 1: Delete all services in subcollection
        db.collection(COLLECTION_PROVIDERS)
                .document(providerId)
                .collection("services")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Step 2: Delete the provider document itself
                    db.collection(COLLECTION_PROVIDERS)
                            .document(providerId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Provider and services deleted: " + providerId);
                                listener.onSuccess("Provider and services deleted");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting provider document", e);
                                listener.onError("Failed to delete provider document: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting services subcollection", e);
                    listener.onError("Failed to delete services: " + e.getMessage());
                });
    }


    /// ///////////////////////////////////////////////////////////////////////////////////////////
    public void cloneServices(String fromProviderId, String toProviderId, OnProviderOperationListener listener) {
        db.collection(COLLECTION_PROVIDERS)
                .document(fromProviderId)
                .collection("services")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        listener.onSuccess("No services to clone");
                        return;
                    }

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Map<String, Object> serviceData = doc.getData();
                        if (serviceData != null) {
                            db.collection(COLLECTION_PROVIDERS)
                                    .document(toProviderId)
                                    .collection("services")
                                    .document(doc.getId())
                                    .set(serviceData);
                        }
                    }
                    listener.onSuccess("Services cloned");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to clone services", e);
                    listener.onError("Failed to clone services: " + e.getMessage());
                });
    }


}