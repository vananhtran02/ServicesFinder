package edu.sjsu.android.servicesfinder.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.android.servicesfinder.model.ServiceReview;

public class ServiceReviewDatabase {

    private static final String COLLECTION_REVIEWS = "serviceReviews";

    private final FirebaseFirestore db;

    public ServiceReviewDatabase() {
        // dùng FirestoreHelper cho đồng bộ config với phần còn lại
        this.db = FirestoreHelper.getInstance();
    }

    // Listener to return review list
    public interface OnReviewsLoadedListener {
        void onReviewsLoaded(List<ServiceReview> reviews);
        void onError(Exception e);
    }

    // Listener when done save review
    public interface OnReviewSavedListener {
        void onReviewSaved();
        void onError(Exception e);
    }

    /**
     * Get all reviews for 1 serviceId, sort based timestamp mới → cũ
     */
    public void getReviewsForService(@NonNull String serviceId,
                                     @NonNull OnReviewsLoadedListener listener) {

        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("serviceId", serviceId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ServiceReview> result = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ServiceReview review = doc.toObject(ServiceReview.class);
                        if (review != null) {
                            review.setId(doc.getId());
                            result.add(review);
                        }
                    }
                    listener.onReviewsLoaded(result);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * And new review to Firestore
     */
    public void addReview(@NonNull ServiceReview review,
                          @NonNull OnReviewSavedListener listener) {

        Task<?> task = db.collection(COLLECTION_REVIEWS)
                .add(review)
                .addOnSuccessListener(docRef -> listener.onReviewSaved())
                .addOnFailureListener(listener::onError);
    }
}
