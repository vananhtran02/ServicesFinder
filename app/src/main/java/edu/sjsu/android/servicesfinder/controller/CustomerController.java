package edu.sjsu.android.servicesfinder.controller;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import edu.sjsu.android.servicesfinder.R;
import edu.sjsu.android.servicesfinder.database.CustomerDatabase;
import edu.sjsu.android.servicesfinder.database.FirestoreHelper;
import edu.sjsu.android.servicesfinder.model.Customer;

/**
 * CUSTOMERCONTROLLER
 * Handles:
 * - Customer registration (FirebaseAuth or Firestore)
 * - Customer login validation
 * - Customer data loading
 * - Favorite providers management
 * - Input validation and sanitization
 */
public class CustomerController {

    private final CustomerDatabase customerDatabase;
    private final FirebaseAuth auth;
    private CustomerControllerListener listener;
    private final Context context;

    public CustomerController(Context context) {
        this.customerDatabase = new CustomerDatabase(context);
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public void setListener(CustomerControllerListener listener) {
        this.listener = listener;
    }

    public CustomerControllerListener getListener() {
        return listener;
    }

    /* ****************************************************************************************
     * REGISTER CUSTOMER
     ****************************************************************************************/
    public void registerCustomer(Customer customer, CustomerDatabase.OnCustomerOperationListener debugFlow) {
        customer.setFullName(FirestoreHelper.sanitizeString(customer.getFullName()));
        customer.setEmail(FirestoreHelper.sanitizeString(customer.getEmail()));
        customer.setPhone(FirestoreHelper.sanitizeString(customer.getPhone()));

        customerDatabase.addCustomer(customer, new CustomerDatabase.OnCustomerOperationListener() {
            @Override
            public void onSuccess(String message) {
                debugFlow.onSuccess(message);
            }

            @Override
            public void onError(String errorMessage) {
                debugFlow.onError(errorMessage);
            }
        });
    }

    /* ****************************************************************************************
     * LOAD CUSTOMER
     ******************************************************************************************/
    public void loadCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            String msg = "Customer ID is required";
            if (listener != null) listener.onError(msg);
            return;
        }

        customerDatabase.getCustomerById(customerId, new CustomerDatabase.OnCustomerLoadedListener() {
            @Override
            public void onSuccess(Customer customer) {
                if (listener != null) listener.onCustomerLoaded(customer);
            }

            @Override
            public void onError(String errorMessage) {
                if (listener != null) listener.onError(errorMessage);
            }
        });
    }

    /* *****************************************************************************************
     * SIGN-UP
     ******************************************************************************************/
    public void signUp(String fullName, String email, String phone, String password) {

        if (email != null && !email.trim().isEmpty()) {
            // Normal Firebase email sign-up
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        assert auth.getCurrentUser() != null;
                        String uid = auth.getCurrentUser().getUid();
                        createAndSaveCustomer(uid, fullName, email, phone);
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) {
                            String signupErMsg = context.getString(R.string.sign_up_failed);
                            String errorMsg = signupErMsg + e.getMessage();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                errorMsg = context.getString(R.string.email_already_in_use);
                            }
                            listener.onError(errorMsg);
                        }
                    });

        } else {
            // Phone-only sign-up (Firestore only, no FirebaseAuth)
            String fakeUid = "C_" + System.currentTimeMillis();
            createAndSaveCustomer(fakeUid, fullName, "", phone);
        }
    }

    /* *****************************************************************************************
     * SIGN-IN
     ******************************************************************************************/
    public interface AuthCallback {
        void onSuccess(String customerId);
        void onError(String message);
    }

    /* *****************************************************************************************
     * Email-based sign-in via FirebaseAuth
     /* *****************************************************************************************/
    public void signInWithEmail(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    if (result.getUser() != null)
                        callback.onSuccess(result.getUser().getUid());
                    else
                        callback.onError(context.getString(R.string.error_null_user));
                })
                .addOnFailureListener(e -> callback.onError(context.getString(R.string.error_email_signin_failed, e.getMessage())));
    }

    /* *****************************************************************************************
     * Phone-based sign-in via Firestore
     ******************************************************************************************/
    public void signInWithPhone(String phone, AuthCallback callback) {
        phone = phone.replaceAll("[^0-9]", "");
        customerDatabase.getCustomerByPhone(phone, new CustomerDatabase.OnCustomerLoadedListener() {
            @Override
            public void onSuccess(Customer customer) {
                callback.onSuccess(customer.getId());
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /* *****************************************************************************************
     * Helper: Create and save customer to Firestore
     ******************************************************************************************/
    private void createAndSaveCustomer(String uid, String fullName, String email, String phone) {
        Customer customer = new Customer(uid, fullName, email, phone);

        registerCustomer(customer, new CustomerDatabase.OnCustomerOperationListener() {
            @Override
            public void onSuccess(String message) {
                if (listener != null) listener.onCustomerRegistered(uid);
            }

            @Override
            public void onError(String errorMessage) {
                if (listener != null) listener.onError(errorMessage);
            }
        });
    }

    /* *****************************************************************************************
     * FAVORITE PROVIDERS MANAGEMENT
     ******************************************************************************************/

    /**
     * Add a provider to customer's favorites
     */
    public void addFavoriteProvider(String customerId, String providerId, CustomerDatabase.OnCustomerOperationListener callback) {
        customerDatabase.addFavoriteProvider(customerId, providerId, callback);
    }

    /**
     * Remove a provider from customer's favorites
     */
    public void removeFavoriteProvider(String customerId, String providerId, CustomerDatabase.OnCustomerOperationListener callback) {
        customerDatabase.removeFavoriteProvider(customerId, providerId, callback);
    }

    /**
     * Toggle favorite status
     */
    public void toggleFavoriteProvider(String customerId, String providerId, boolean isFavorite, CustomerDatabase.OnCustomerOperationListener callback) {
        if (isFavorite) {
            removeFavoriteProvider(customerId, providerId, callback);
        } else {
            addFavoriteProvider(customerId, providerId, callback);
        }
    }

    /* *****************************************************************************************
     * UPDATE CUSTOMER PROFILE
     ******************************************************************************************/
    public void updateCustomerProfile(String customerId, String fullName, String phone, CustomerDatabase.OnCustomerOperationListener callback) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();

        if (fullName != null && !fullName.trim().isEmpty()) {
            updates.put("fullName", FirestoreHelper.sanitizeString(fullName));
        }

        if (phone != null && !phone.trim().isEmpty()) {
            updates.put("phone", FirestoreHelper.sanitizeString(phone));
        }

        if (updates.isEmpty()) {
            callback.onError("No updates to perform");
            return;
        }

        customerDatabase.updateCustomerFields(customerId, updates, callback);
    }

    /* *****************************************************************************************
     * DELETE CUSTOMER ACCOUNT
     ******************************************************************************************/
    public void deleteCustomerAccount(String customerId, CustomerDatabase.OnCustomerOperationListener callback) {
        customerDatabase.deleteCustomer(customerId, new CustomerDatabase.OnCustomerOperationListener() {
            @Override
            public void onSuccess(String message) {
                // Also delete from FirebaseAuth if email-based account
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(message))
                            .addOnFailureListener(e -> callback.onError("Customer data deleted, but auth failed: " + e.getMessage()));
                } else {
                    callback.onSuccess(message);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /* *****************************************************************************************
     * LISTENER INTERFACE
     ******************************************************************************************/
    public interface CustomerControllerListener {
        void onCustomerLoaded(Customer customer);
        void onCustomerRegistered(String customerId);
        void onError(String errorMessage);
    }
}
