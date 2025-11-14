package edu.sjsu.android.servicesfinder.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;
import edu.sjsu.android.servicesfinder.R;
import edu.sjsu.android.servicesfinder.controller.ProviderController;
import edu.sjsu.android.servicesfinder.database.ProviderDatabase;
import edu.sjsu.android.servicesfinder.model.Provider;

//**************************************************************************************************
// EditProfileActivity   (used with edit_profile_setting.xml)
//**************************************************************************************************
 /**
 * EditProfileActivity allows providers to view and update their personal information,
 * full name, email, phone number, and address.
 *
 * * Dependencies: ProviderController, ProviderDatabase, FirebaseAuth
 */
//**************************************************************************************************

public class EditProfileActivity extends AppCompatActivity {

    // Input fields for profile editing
    private TextInputEditText fullNameInput, emailInput, phoneInput, addressInput;

    // Controller to handle provider logic
    private ProviderController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_setting);

        // Setup toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.editProfileToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Initialize input fields
        fullNameInput = findViewById(R.id.editProfileFullName);
        emailInput = findViewById(R.id.editProfileEmail);
        phoneInput = findViewById(R.id.editProfilePhone);
        addressInput = findViewById(R.id.editProfileAddress);

        // Initialize buttons
        Button saveButton = findViewById(R.id.editProfileSaveButton);
        Button cancelButton = findViewById(R.id.editProfileCancelButton);

        // Initialize controller and set listener for callbacks
        controller = new ProviderController(this);
        controller.setListener(new ProviderController.ProviderControllerListener() {
            @Override
            public void onSignUpSuccess(String message) {
                // Called when profile update or migration succeeds
                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String msg) {
                // Called when any error occurs
                Toast.makeText(EditProfileActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderLoaded(Provider provider) {
                // Populate UI with loaded provider data
                fullNameInput.setText(provider.getFullName());
                emailInput.setText(provider.getEmail());
                phoneInput.setText(provider.getPhone());
                addressInput.setText(provider.getAddress());
            }
        });

        // Load current provider info from Firestore
        loadCurrentProvider();

        // Save button logic
        saveButton.setOnClickListener(v -> {
            // Extract input values
            String name = Objects.requireNonNull(fullNameInput.getText()).toString().trim();
            String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
            String phone = Objects.requireNonNull(phoneInput.getText()).toString().trim();
            String address = Objects.requireNonNull(addressInput.getText()).toString().trim();

            // Get current Firebase user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show();
                return;
            }

            String oldEmail = currentUser.getEmail();
            String oldUid = currentUser.getUid();

            // If email changed, trigger full migration
            if (!email.equals(oldEmail)) {
                controller.cloneProviderWithServices(
                        oldUid,
                        email,
                        name,
                        phone,
                        address,
                        new ProviderController.ProviderControllerListener() {
                            @Override
                            public void onSignUpSuccess(String providerId) {
                                // Called when migration completes
                                Toast.makeText(EditProfileActivity.this, "New account created with services cloned", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                // Called when migration fails
                                Toast.makeText(EditProfileActivity.this, "Migration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onProviderLoaded(Provider provider) {
                                // Not used in this context
                            }
                        }
                );
            } else {
                // If email unchanged, just update profile
                controller.updateProfile(name, email, phone, address, null, controller.getListener());
            }
        });

        //  Cancel button closes activity
        cancelButton.setOnClickListener(v -> finish());
    }

    //  Load current provider data from Firestore
    private void loadCurrentProvider() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ProviderDatabase providerDatabase = new ProviderDatabase();
            providerDatabase.getProviderById(user.getUid(), new ProviderDatabase.OnProviderLoadedListener() {
                @Override
                public void onSuccess(Provider provider) {
                    // Populate fields with provider data
                    fullNameInput.setText(provider.getFullName());
                    emailInput.setText(provider.getEmail());
                    phoneInput.setText(provider.getPhone());
                    addressInput.setText(provider.getAddress());
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Handle toolbar back navigation
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
