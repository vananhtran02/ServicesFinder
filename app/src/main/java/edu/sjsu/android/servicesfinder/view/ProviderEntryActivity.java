package edu.sjsu.android.servicesfinder.view;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;
import edu.sjsu.android.servicesfinder.R;
import edu.sjsu.android.servicesfinder.controller.SessionManager;
import edu.sjsu.android.servicesfinder.controller.ProviderController;
import edu.sjsu.android.servicesfinder.model.Provider;

/**
 * ProviderEntryActivity
 * ------------------------------------------------------------
 * Handles both Sign-In and Sign-Up for Providers.
 * Supports:
 *  - Signup with Email optional
 *  - Sign in with Email or Phone
 *
 *  Call: ProviderController for logic & database actions
 *  Use: activity_provider_entry.xml
 *
 */
public class ProviderEntryActivity extends AppCompatActivity
        implements ProviderController.ProviderControllerListener {

    /* -------------------------- UI COMPONENTS -------------------------- */
    private TabLayout tabLayout;
    private View signInLayout, signUpLayout;

    // Sign-In fields
    private TextInputEditText signInEmailOrPhone, signInPassword;
    private Button signInButton, signInCancelButton;

    // Sign-Up fields
    private TextInputEditText signUpFullName, signUpEmail, signUpPhone;
    private TextInputEditText signUpAddress, signUpPassword, signUpConfirmPassword;
    private Button signUpButton, signUpCancelButton;

    /* -------------------------- CONTROLLER ----------------------------- */
    private ProviderController providerController;
    private ProgressDialog loadingDialog;

    /* -------------------------- LIFECYCLE ------------------------------ */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_entry);
        setTitle("Provider Authentication");

        // Initialize controller (acts as business logic layer)
        providerController = new ProviderController(this);
        providerController.setListener(this);

        // Prepare UI
        initializeViews();
        setupTabs();
        setupButtons();

        // Default tab = Sign-In
        showSignIn();
    }

    /* =========================================================
       INITIALIZATION METHODS
       ========================================================= */
    private void initializeViews() {
        tabLayout = findViewById(R.id.authTabLayout);
        signInLayout = findViewById(R.id.signInLayout);
        signUpLayout = findViewById(R.id.signUpLayout);

        // Sign-In views
        signInEmailOrPhone = findViewById(R.id.signInEmailOrPhone);
        signInPassword = findViewById(R.id.signInPassword);
        signInButton = findViewById(R.id.signInButton);
        signInCancelButton = findViewById(R.id.signInCancelButton);

        // Sign-Up views
        signUpFullName = findViewById(R.id.signUpFullName);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPhone = findViewById(R.id.signUpPhone);
        signUpAddress = findViewById(R.id.signUpAddress);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpConfirmPassword = findViewById(R.id.signUpConfirmPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signUpCancelButton = findViewById(R.id.signUpCancelButton);
    }

    private void setupTabs() {
        // Toggle between Sign-In and Sign-Up layouts
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) showSignIn(); else showSignUp();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtons() {
        signInButton.setOnClickListener(v -> handleSignIn());
        signUpButton.setOnClickListener(v -> handleSignUp());
        signInCancelButton.setOnClickListener(v -> finish());
        signUpCancelButton.setOnClickListener(v -> finish());
    }

    /* =========================================================
       TAB SWITCHING
       ========================================================= */
    private void showSignIn() {
        signInLayout.setVisibility(View.VISIBLE);
        signUpLayout.setVisibility(View.GONE);
    }

    private void showSignUp() {
        signInLayout.setVisibility(View.GONE);
        signUpLayout.setVisibility(View.VISIBLE);
    }

    /* =========================================================
       SIGN-IN LOGIC
       ========================================================= */
    private void handleSignIn() {
        String emailOrPhone = getText(signInEmailOrPhone);
        String password = getText(signInPassword);

        // Validation
        if (TextUtils.isEmpty(emailOrPhone)) {
            signInEmailOrPhone.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            signInPassword.setError("Required");
            return;
        }

        Log.d(TAG, "handleSignIn(): emailOrPhone=" + emailOrPhone);

        showLoading("Signing in...");

        // EMAIL LOGIN
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            Log.d(TAG, "handleSignIn(): detected EMAIL login");
            providerController.signInWithEmail(emailOrPhone, password,
                    new ProviderController.AuthCallback() {
                        @Override
                        public void onSuccess(String providerId) {
                            Log.d(TAG, "signInWithEmail SUCCESS, providerId=" + providerId);
                            hideLoading();
                            // now load provider doc from Firestore
                            providerController.loadProviderById(providerId);
                        }

                        @Override
                        public void onError(String msg) {
                            Log.e(TAG, "signInWithEmail ERROR: " + msg, new Exception("signInWithEmail"));
                            hideLoading();
                            // call the Activity's onError (this WON'T recurse now)
                            ProviderEntryActivity.this.onError(msg);
                        }
                    });
        } else {
            // PHONE LOGIN
            Log.d(TAG, "handleSignIn(): detected PHONE login");
            providerController.signInWithPhone(emailOrPhone, password,
                    new ProviderController.AuthCallback() {
                        @Override
                        public void onSuccess(String providerId) {
                            Log.d(TAG, "signInWithPhone SUCCESS, providerId=" + providerId);
                            hideLoading();
                            providerController.loadProviderById(providerId);
                        }

                        @Override
                        public void onError(String msg) {
                            Log.e(TAG, "signInWithPhone ERROR: " + msg, new Exception("signInWithPhone"));
                            hideLoading();
                            ProviderEntryActivity.this.onError(msg);
                        }
                    });
        }
    }


    /* =========================================================
       SIGN-UP LOGIC (EMAIL OPTIONAL)
       ========================================================= */
    private void handleSignUp() {
        String fullName = getText(signUpFullName);
        String email    = getText(signUpEmail);
        String phone    = getText(signUpPhone);
        String address  = getText(signUpAddress);
        String password = getText(signUpPassword);
        String confirm  = getText(signUpConfirmPassword);

        // Validate all inputs
        if (!validateSignUpInputs(fullName, email, phone, address, password, confirm))
            return;

        showLoading("Creating account...");

        // Sign up
        providerController.signUp(fullName, email, phone, address, password);
    }

    /* =========================================================
       INPUT VALIDATION
       ========================================================= */
    private boolean validateSignUpInputs(String fullName, String email, String phone,
                                         String address, String password, String confirm) {
        if (TextUtils.isEmpty(fullName)) { signUpFullName.setError("Required"); return false; }
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpEmail.setError("Invalid email"); return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() != 10) { signUpPhone.setError("10 digits"); return false; }
        if (TextUtils.isEmpty(address)) { signUpAddress.setError("Required"); return false; }
        if (password.length() < 6) { signUpPassword.setError("Min 6 chars"); return false; }
        if (!password.equals(confirm)) { signUpConfirmPassword.setError("Mismatch"); return false; }
        return true;
    }

    /* =========================================================
       UTILITIES & HELPERS
       ========================================================= */
    private String getText(TextInputEditText edit) {
        Editable e = edit.getText();
        return e != null ? e.toString().trim() : "";
    }

    private void showLoading(String msg) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setCancelable(false);
        }
        loadingDialog.setMessage(msg);
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    /* =========================================================
       CONTROLLER CALLBACK IMPLEMENTATION
       ========================================================= */
    @Override
    public void onProviderLoaded(Provider provider) {
        hideLoading();
        Log.d("ProviderEntryActivity", "Provider loaded: " + provider.getId());

        // Save provider session
        SessionManager.saveProvider(this, provider.getId(), provider.getFullName());

        // Wrap navigation in try-catch to isolate crash
        try {
            navigateToDashboard(provider);
        } catch (Exception e) {
            Log.e("NavigationError", "Failed to navigate to dashboard", e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSignUpSuccess(String providerId) {
        hideLoading();
        clearSignUpForm();
        launchProviderDashboard();
    }

    @Override
    public void onError(String msg) {
        Log.d("onError", "Entered onError()");
        hideLoading();

        try {
            String safeMsg = msg != null ? msg : "Unknown error";
            Toast.makeText(ProviderEntryActivity.this, "EEEEEEError: " + safeMsg, Toast.LENGTH_LONG).show();
            Log.d("onError", "Toast shown");
        } catch (Exception e) {
            Log.e("onError", "Toast crashed", e);
        }
    }

    /* =========================================================
       NAVIGATION HELPERS
       ========================================================= */
    private void navigateToDashboard(Provider provider) {
        Log.d("ProviderEntryActivity", "Navigating to dashboard for: " + provider.getFullName());
        Intent i = new Intent(this, ProviderDashboardActivity.class);
        i.putExtra("providerId", provider.getId());
        i.putExtra("providerName", provider.getFullName());
        startActivity(i);
    }

    private void clearSignUpForm() {
        signUpFullName.setText("");
        signUpEmail.setText("");
        signUpPhone.setText("");
        signUpAddress.setText("");
        signUpPassword.setText("");
        signUpConfirmPassword.setText("");
    }

    private void launchProviderDashboard() {
        Intent intent = new Intent(this, ProviderDashboardActivity.class);
        startActivity(intent);
        finish();
    }

}