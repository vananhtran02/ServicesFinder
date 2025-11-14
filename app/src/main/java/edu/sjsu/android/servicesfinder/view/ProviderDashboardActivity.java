    package edu.sjsu.android.servicesfinder.view;

    import android.app.Dialog;
    import android.app.ProgressDialog;
    import android.content.Intent;
    import android.graphics.Typeface;
    import android.net.Uri;
    import android.os.Bundle;
    import android.text.InputType;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.Window;
    import android.view.WindowManager;
    import android.widget.*;
    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;
    import androidx.core.content.FileProvider;
    import com.bumptech.glide.Glide;
    import com.google.android.material.dialog.MaterialAlertDialogBuilder;
    import com.google.android.material.textfield.MaterialAutoCompleteTextView;
    import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import java.io.File;
    import java.util.*;
    import edu.sjsu.android.servicesfinder.R;
    import edu.sjsu.android.servicesfinder.controller.FormHelper;
    import edu.sjsu.android.servicesfinder.controller.ProviderController;
    import edu.sjsu.android.servicesfinder.controller.SessionManager;
    import edu.sjsu.android.servicesfinder.controller.CatalogueController;
    import edu.sjsu.android.servicesfinder.controller.ProviderServiceController;
    import edu.sjsu.android.servicesfinder.controller.UIHelper;
    import edu.sjsu.android.servicesfinder.database.ProviderDatabase;
    import edu.sjsu.android.servicesfinder.database.ProviderServiceDatabase;
    import edu.sjsu.android.servicesfinder.database.StorageHelper;
    import edu.sjsu.android.servicesfinder.model.Catalogue;
    import edu.sjsu.android.servicesfinder.model.Provider;
    import edu.sjsu.android.servicesfinder.model.ProviderService;
    import edu.sjsu.android.servicesfinder.model.Service;

    /*******************************************************************************************************
     * ProviderDashboardActivity
     *
     * This screen is displayed after a provider signs in or log in.
     *
     * Responsibilities:
     *  - Load previous “draft” automatically when signIn
     *  - Allow the provider to create and save a service.
     *  - Upload a service image to Firebase Storage
     *  - Save service details to Firebase Firestore.
     *
     * Firebase components used:
     *      Firestore → stores structured service data
     *      Storage   → stores uploaded image files
     */
    public class ProviderDashboardActivity extends AppCompatActivity
            implements CatalogueController.CatalogueControllerListener {

        private static final String TAG = "ProviderDashboard";

        // UI Components
        private ImageView imagePreview;
        private TextInputEditText serviceTitleInput, descriptionInput, pricingInput;
        private TextView catalogueTextView;
        // private Spinner serviceAreaSpinner;
        private MaterialAutoCompleteTextView serviceAreaSpinner;
        private RadioGroup contactPreferenceGroup;
        private ProgressDialog loadingDialog;
        private CheckBox mon, tue, wed, thu, fri, sat, sun;

        // Controllers and Firebase
        private CatalogueController catalogueController;
        private FirebaseFirestore firestore;
        private StorageReference storageRef;

        // State
        private Uri selectedImageUri, tempImageUri;
        private MultiSelectDropdown catalogueDropdown;
        private boolean cataloguesLoaded = false;
        private String editingServiceId = null;

        private ProviderServiceController providerServiceController;

        private ProviderController controller;


        // Image selection launchers
        private final ActivityResultLauncher<Intent> galleryLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Log.d(TAG, "Image selected from gallery: " + selectedImageUri);
                        Glide.with(this).load(selectedImageUri).into(imagePreview);
                        imagePreview.setVisibility(View.VISIBLE);
                    }
                });

        private final ActivityResultLauncher<Uri> cameraLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success) {
                        selectedImageUri = tempImageUri;
                        Log.d(TAG, "Photo captured: " + selectedImageUri);
                        Glide.with(this).load(selectedImageUri).into(imagePreview);
                        imagePreview.setVisibility(View.VISIBLE);
                    }
                });

        // =========================================================
        // LIFECYCLE METHODS
        // =========================================================

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_provider_dashboard);
            setTitle("Provider Dashboard");

            // Initialize Firebase
            firestore = FirebaseFirestore.getInstance();
            storageRef = FirebaseStorage.getInstance().getReference();

            // Initialize controller
            catalogueController = new CatalogueController();
            catalogueController.setListener(this);

            providerServiceController = new ProviderServiceController();
            controller = new ProviderController(this);



            // Setup UI
            initializeViews();
            catalogueTextView.setEnabled(false);
            catalogueTextView.setText("Loading catalogues...");
            catalogueDropdown = new MultiSelectDropdown(this, catalogueTextView, new HashMap<>());

            // Load initial data
            showLoadingDialog();
            loadServiceAreas();
            loadCatalogues();
            setupButtons();

            // Set default contact preference
            ((RadioButton) findViewById(R.id.contactCall)).setChecked(true);

            // For Setting
            initializeSettingsIcons();

        }

        /**
         * initializeViews()
         *
         * This method binds each UI widget (defined in XML) to a Java variable.
         * We must call findViewById() AFTER setContentView(), otherwise they will be null.
         *
         * These references allow us to read user input, show errors, and update UI state.
         */
        private void initializeViews() {
            serviceTitleInput = findViewById(R.id.serviceTitleInput);
            descriptionInput = findViewById(R.id.descriptionInput);
            pricingInput = findViewById(R.id.pricingInput);
            catalogueTextView = findViewById(R.id.catalogueDropdown);
            imagePreview = findViewById(R.id.imagePreview);
            serviceAreaSpinner = findViewById(R.id.serviceAreaSpinner);
            contactPreferenceGroup = findViewById(R.id.contactPreferenceGroup);

            mon = findViewById(R.id.mon);
            tue = findViewById(R.id.tue);
            wed = findViewById(R.id.wed);
            thu = findViewById(R.id.thu);
            fri = findViewById(R.id.fri);
            sat = findViewById(R.id.sat);
            sun = findViewById(R.id.sun);
        }

        private void setupButtons() {
            Button uploadImageBtn = findViewById(R.id.uploadImageBtn);
            Button saveBtn = findViewById(R.id.saveBtn);
            Button cancelBtn = findViewById(R.id.cancelBtn);

            uploadImageBtn.setOnClickListener(v -> showImagePickerDialog());
            saveBtn.setOnClickListener(v -> handleSave());
            cancelBtn.setOnClickListener(v -> handleCancel());
        }

            // =========================================================
            // SAVE SERVICE
            // =========================================================
            /*
             * Called when user taps the "Save" button.
             * Steps:
             *   1. Collect all user inputs.
             *   2. Validate required fields.
             *   3. Format selected dropdown items into a category summary string.
             *   4. If an image was selected → upload to Firebase Storage.
             *   5. Save metadata to Firestore under: providers/{providerID}/services/{autoGeneratedDocID}
             */
            private void handleSave() {
                Log.e(TAG, "========== HANDLE SAVE CALLED ==========");
                System.out.println("========== HANDLE SAVE CALLED ==========");

                // 1. Safely get providerId (works for both email + phone sign-ups)
                String providerId = SessionManager.getProviderId(this);

                if (providerId == null) {
                    providerId = getIntent().getStringExtra("providerId");
                    if (providerId != null) {
                        SessionManager.saveProvider(this, providerId, getIntent().getStringExtra("providerName"));
                        Log.w(TAG, "Recovered providerId from Intent: " + providerId);
                    }
                }

                if (providerId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    providerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    SessionManager.saveProvider(this, providerId, SessionManager.getProviderName(this));
                    Log.w(TAG, "Recovered providerId from FirebaseAuth: " + providerId);
                }

                if (providerId == null) {
                    Log.e(TAG, "providerId is null. Aborting save.");
                    Toast.makeText(this, "No provider ID found. Please log in again.", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d(TAG, "Using providerId: " + providerId);

                // 2. Collect user inputs
                String title = FormHelper.getText(serviceTitleInput);
                String description = FormHelper.getText(descriptionInput);
                String pricing = FormHelper.getText(pricingInput);
                String area = FormHelper.getSelectedItem(serviceAreaSpinner);
                String availability = FormHelper.getSelectedAvailability(mon, tue, wed, thu, fri, sat, sun);
                String contactPreference = FormHelper.getSelectedContactPreference(contactPreferenceGroup, this);
                Map<String, Set<String>> selectedItems = catalogueDropdown.getSelectedItems();
                String category = FormHelper.formatCategoryFromSelection(selectedItems);


                // 3. Log inputs for debugging
                Log.d(TAG, "=== SAVE SERVICE DEBUG ===");
                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Description: " + description);
                Log.d(TAG, "Pricing: " + pricing);
                Log.d(TAG, "Area: " + area);
                Log.d(TAG, "Availability: " + availability);
                Log.d(TAG, "Contact Preference: " + contactPreference);
                Log.d(TAG, "Category: " + category);
                Log.d(TAG, "Image URI: " + selectedImageUri);
                Log.d(TAG, "Provider ID: " + providerId);
                Log.d(TAG, "==========================");

                // 4. Validate inputs
                if (title.isEmpty()) {
                    serviceTitleInput.setError("Required");
                    Toast.makeText(this, "Please enter service title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.isEmpty()) {
                    descriptionInput.setError("Required");
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pricing.isEmpty()) {
                    pricingInput.setError("Required");
                    Toast.makeText(this, "Please enter pricing details", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (area.equals("Select Service Area") || area.isEmpty()) {
                    Toast.makeText(this, "Please select service area", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (availability.isEmpty()) {
                    Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (contactPreference.isEmpty()) {
                    Toast.makeText(this, "Please select contact preference", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (category.isEmpty()) {
                    Toast.makeText(this, "Please select catalogue & services", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 5. Handle image upload or direct save
                if (selectedImageUri != null) {
                    String uriString = selectedImageUri.toString();

                    // Already uploaded image (HTTP/HTTPS)
                    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                        Log.d(TAG, "Image already on Firebase, skipping upload");
                        saveServiceToFirestore(title, description, pricing, category,
                                area, availability, contactPreference, providerId, uriString);
                    } else {

                        Log.d(TAG, "Uploading new image to Firebase...");
                        String finalProviderId = providerId;
                        StorageHelper.uploadImageToFirebase(this, selectedImageUri, providerId, imageUrl -> {
                            saveServiceToFirestore(title, description, pricing, category,
                                    area, availability, contactPreference, finalProviderId, imageUrl);
                        });
                    }
                } else {
                    Log.d(TAG, "No image selected — ask user confirmation");
                    String finalProviderId1 = providerId;
                    new AlertDialog.Builder(this)
                            .setTitle("No Image")
                            .setMessage("Do you want to add a service without an image?")
                            .setPositiveButton("Yes, Continue", (dialog, which) -> {
                                saveServiceToFirestore(title, description, pricing, category,
                                        area, availability, contactPreference, finalProviderId1, null);
                            })
                            .setNegativeButton("Add Image", (dialog, which) -> {
                                showImagePickerDialog();
                            })
                            .show();
                }
            }
            // =========================================================
            // GET ERROR MSG WHEN UPLOAD IMAGE
            // =========================================================
             /* Case:
             *  • permission denied → check Storage rules
             *  • network unavailable → check WiFi/mobile data
             */
            private String getImageUploadErrorMessage(Exception e) {
                String message = e.getMessage();

                if (message == null) {
                    return "Unknown upload error";
                }
                if (message.contains("permission") || message.contains("PERMISSION_DENIED")) {
                    return "Permission denied. Please check Firebase Storage rules.";
                } else if (message.contains("network") || message.contains("UNAVAILABLE")) {
                    return "Network error. Check your internet connection.";
                } else if (message.contains("quota")) {
                    return "Storage quota exceeded.";
                } else if (message.contains("unauthorized") || message.contains("UNAUTHENTICATED")) {
                    return "Not authorized. Please sign in again.";
                } else {
                    return "Upload failed: " + message;
                }
            }

        // =========================================================
        // SAVE SERVICE TO FIRESTORE
        // =========================================================
        private void saveServiceToFirestore(String title, String description, String pricing,
                                            String category, String area, String availability,
                                            String contactPreference, String providerId, String imageUrl) {

            Log.e("ProviderDashboard", "=== DEBUG: saveServiceToFirestore() called ===");

            // STEP 1: Defensive check
            if (providerId == null || providerId.isEmpty()) {
                Log.e("ProviderDashboard", "ERROR: providerId is NULL. Aborting save.");
                Toast.makeText(this, "Error: No provider ID found (login/session issue).", Toast.LENGTH_LONG).show();
                return;
            }

            // STEP 2: Create model and fill data
            ProviderService service = new ProviderService();
            service.setProviderId(providerId);
            service.setServiceTitle(title);
            service.setDescription(description);
            service.setPricing(pricing);
            service.setCategory(category);
            service.setServiceArea(area);
            service.setAvailability(availability);
            service.setContactPreference(contactPreference);
            service.setImageUrl(imageUrl != null ? imageUrl : "");
            service.setStatus("Active");
            service.setRating(0.0);
            service.setTimestamp(System.currentTimeMillis());
            if (editingServiceId != null) service.setId(editingServiceId);

            // STEP 3: Log all values for confirmation
            Log.d("ProviderDashboard", "=== SERVICE DEBUG INFO ===");
            Log.d("ProviderDashboard", "Title: " + title);
            Log.d("ProviderDashboard", "Description: " + description);
            Log.d("ProviderDashboard", "Pricing: " + pricing);
            Log.d("ProviderDashboard", "Category: " + category);
            Log.d("ProviderDashboard", "Area: " + area);
            Log.d("ProviderDashboard", "Availability: " + availability);
            Log.d("ProviderDashboard", "ContactPref: " + contactPreference);
            Log.d("ProviderDashboard", "ImageUrl: " + imageUrl);
            Log.d("ProviderDashboard", "ProviderID: " + providerId);
            Log.d("ProviderDashboard", "==========================");

            // STEP 4: Show loading dialog
            ProgressDialog savingDialog = new ProgressDialog(this);
            savingDialog.setMessage("Saving service...");
            savingDialog.setCancelable(false);
            savingDialog.show();

            // STEP 5: Save to Firestore via controller
            ProviderServiceController controller = new ProviderServiceController();
            controller.saveOrUpdateService(providerId, service, new ProviderServiceDatabase.OnServiceSaveListener() {
                @Override
                public void onSuccess(String serviceId) {
                    savingDialog.dismiss();
                    Toast.makeText(ProviderDashboardActivity.this, "Service saved successfully!", Toast.LENGTH_SHORT).show();
                    Log.i("ProviderDashboard", "Service saved successfully! Firestore docId: " + serviceId);
                    clearForm();
                    finish();
                }

                @Override
                public void onError(String error) {
                    savingDialog.dismiss();
                    Log.e("ProviderDashboard", "Service save failed: " + error);
                    Toast.makeText(ProviderDashboardActivity.this, "Save failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        // =========================================================
        // IMAGE PICKER
        // =========================================================
        private void showImagePickerDialog() {
            new AlertDialog.Builder(this)
                    .setTitle("Select Image")
                    .setItems(new String[]{"Choose from Gallery", "Take a Photo"}, (dialog, which) -> {
                        if (which == 0) {
                            openGallery();
                        } else if (which == 1) {
                            openCamera();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }

        private void openGallery() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(Intent.createChooser(intent, "Select a photo"));
        }

        private void openCamera() {
            tempImageUri = createTempImageUri();
            cameraLauncher.launch(tempImageUri);
        }

        private Uri createTempImageUri() {
            File file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        }


        /******************************************************
         * handleCancel()
         * Shows a confirmation dialog when user taps Cancel.
         * Prevents accidental loss of input.
         ********************************************************/
        private void handleCancel() {
            new AlertDialog.Builder(this)
                    .setTitle("Discard changes?")
                    .setMessage("Are you sure you want to cancel without saving?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }

        // =========================================================
        // LOADING DIALOGS
        // =========================================================

        private void showLoadingDialog() {
            if (loadingDialog == null) {
                loadingDialog = new ProgressDialog(this);
                loadingDialog.setCancelable(false);
            }
            loadingDialog.setMessage("Loading catalogues...");
            loadingDialog.show();
        }

        private void hideLoadingDialog() {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }

        // =========================================================
        // LOAD SERVICE AREAS FROM FIREBASE
        // =========================================================

        private void loadServiceAreas() {
            providerServiceController.loadServiceAreas(new ProviderServiceController.ServiceAreaListener() {
                @Override
                public void onLoaded(List<String> areas) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProviderDashboardActivity.this,
                            android.R.layout.simple_spinner_item,
                            areas
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    serviceAreaSpinner.setAdapter(adapter);
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, message);
                    Toast.makeText(ProviderDashboardActivity.this, "Failed to load areas", Toast.LENGTH_SHORT).show();
                }
            });

        }
        // =========================================================
        // LOAD CATALOGUES
        // =========================================================
        private void loadCatalogues() {
            catalogueController.loadCatalogueMapForDropdown();
        }

        @Override
        public void onCataloguesLoaded(List<Catalogue> catalogues) {
            // Not used - we use onCatalogueMapLoaded instead
        }

        @Override
        public void onCatalogueWithServicesLoaded(Catalogue catalogue, List<Service> services) {
            // Not used - we use onCatalogueMapLoaded instead
        }

        @Override
        public void onCatalogueMapLoaded(Map<String, List<String>> catalogueMap) {
            hideLoadingDialog();

            if (catalogueMap.isEmpty()) {
                Toast.makeText(this, "No catalogues available", Toast.LENGTH_LONG).show();
                catalogueTextView.setText("No catalogues available");
                catalogueTextView.setEnabled(false);
                return;
            }

            cataloguesLoaded = true;
            catalogueTextView.setEnabled(true);
            catalogueTextView.setText("Select Catalogue & Services");
            catalogueDropdown.updateCatalogueMap(catalogueMap);

            Toast.makeText(this, "Catalogues loaded", Toast.LENGTH_SHORT).show();

            // Load last service draft after catalogues are ready
            loadLastServiceDraft();
        }

        @Override
        public void onError(String errorMessage) {
            hideLoadingDialog();
            Log.e(TAG, "Controller error: " + errorMessage);
            Toast.makeText(this, "Error loading catalogues: " + errorMessage, Toast.LENGTH_LONG).show();
            catalogueTextView.setText("Failed to load catalogues");
            catalogueTextView.setEnabled(false);
        }

    // =========================================================
    // LOAD LAST SERVICE DRAFT
    // =========================================================

        private void loadLastServiceDraft() {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            Log.d(TAG, "Loading last service draft for provider: " + uid);

            ProviderServiceController controller = new ProviderServiceController();
            controller.loadLastServiceDraft(uid, new ProviderServiceController.OnDraftLoadedListener() {
                @Override
                public void onDraftLoaded(ProviderServiceController.ServiceDraft draft) {
                    // Store the document ID so we can UPDATE it later
                    editingServiceId = draft.getId();
                    Log.d(TAG, "Draft loaded - Document ID: " + editingServiceId);

                    // === Fill text fields ===
                    serviceTitleInput.setText(draft.getServiceTitle());
                    descriptionInput.setText(draft.getDescription());
                    pricingInput.setText(draft.getPricing());

                    // === Service Area ===
                    if (draft.getServiceArea() != null) {
                        UIHelper.setSpinnerSelection(serviceAreaSpinner, draft.getServiceArea());
                    }

                    // === Availability checkboxes ===
                    if (draft.getAvailability() != null) {
                        UIHelper.setAvailabilityCheckboxes(draft.getAvailability(), mon, tue, wed, thu, fri, sat, sun);
                    }

                    // === Contact preference radio buttons ===
                    if (draft.getContactPreference() != null) {
                        UIHelper.setRadioSelection(contactPreferenceGroup, draft.getContactPreference(), ProviderDashboardActivity.this);
                    }

                    // === Restore catalogue dropdown selections ===
                    if (draft.getCategory() != null && !draft.getCategory().isEmpty()) {
                        catalogueDropdown.setSelectedItemsFromCategory(draft.getCategory());
                    }

                    // === Restore image ===
                    if (draft.getImageUrl() != null && !draft.getImageUrl().isEmpty()) {
                        Glide.with(ProviderDashboardActivity.this).load(draft.getImageUrl()).into(imagePreview);
                        imagePreview.setVisibility(View.VISIBLE);
                        selectedImageUri = Uri.parse(draft.getImageUrl());
                    }

                    Toast.makeText(ProviderDashboardActivity.this, "Previous service loaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNoDraftFound() {
                    Log.d(TAG, "No draft found - starting with empty form");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to load draft: " + error);
                    Toast.makeText(ProviderDashboardActivity.this, "Failed to load draft", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // =========================================================
        // Resets all input fields to default state
        // =========================================================
        private void clearForm() {
            serviceTitleInput.setText("");
            descriptionInput.setText("");
            pricingInput.setText("");
            serviceAreaSpinner.setText("", false);
            mon.setChecked(false);
            tue.setChecked(false);
            wed.setChecked(false);
            thu.setChecked(false);
            fri.setChecked(false);
            sat.setChecked(false);
            sun.setChecked(false);
            contactPreferenceGroup.clearCheck();
            ((RadioButton) findViewById(R.id.contactCall)).setChecked(true);
            selectedImageUri = null;
            imagePreview.setVisibility(View.GONE);
            catalogueTextView.setText("Select Catalogue & Services");
            editingServiceId = null;  // Reset editing ID
        }
        // =========================================================
        // SETTINGS ICON & MENU (View Layer)
        // =========================================================
        private void initializeSettingsIcons() {
            ImageButton settingsButton = findViewById(R.id.settingsButton);
            settingsButton.setOnClickListener(v -> showSettingsDialog());
        }

        private void showSettingsDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Inflate account_settings.xml layout
            View dialogView = getLayoutInflater().inflate(R.layout.account_settings, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Handle Up button
            ImageButton upButton = dialogView.findViewById(R.id.upButton);
            upButton.setOnClickListener(v -> dialog.dismiss()); // closes the settings dialog


            // Hook up buttons from XML
            ImageButton changePasswordBtn = dialogView.findViewById(R.id.iconChangePassword);
            ImageButton editProfileBtn = dialogView.findViewById(R.id.iconEditProfile);
            ImageButton deleteAccountBtn = dialogView.findViewById(R.id.iconDeleteAccount);

            // Handle actions
            changePasswordBtn.setOnClickListener(v -> {
                dialog.dismiss();
                openChangePasswordDialog();  // View delegates to controller
            });

            editProfileBtn.setOnClickListener(v -> {
                dialog.dismiss();
                openChangeProfileDialog();
            });

            deleteAccountBtn.setOnClickListener(v -> {
                dialog.dismiss();
                confirmDeleteAccount();
            });
        }
        // =========================================================
        // SETTINGS DIALOGS (View Layer → Controller Calls)
        // =========================================================

        private void openChangePasswordDialog() {
            int padding = (int) (20 * getResources().getDisplayMetrics().density);
            // Create title TextView
            TextView title = new TextView(this);
            title.setText("Change Password");
            title.setTextSize(20);
            title.setTextColor(ContextCompat.getColor(this, R.color.white));
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setPadding(padding, padding, padding, padding);
            // Wrap title in a container with bisque background
            LinearLayout titleContainer = new LinearLayout(this);
            titleContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.design_default_color_primary));
            titleContainer.setPadding(0, 0, 0, 0); // Optional: outer padding
            titleContainer.addView(title);

            // Create input field
            final EditText input = new EditText(this);
            input.setHint("Enter new password");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setPadding(padding, padding, padding, padding);
            input.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

            // Build dialog
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCustomTitle(titleContainer)
                    .setView(input)
                    .setPositiveButton("Update", null) // Set to null to override later
                    .setNegativeButton("Cancel", null)
                    .create();

            // Show dialog and customize buttons
            dialog.setOnShowListener(d -> {
                Button updateBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Set button text colors
                updateBtn.setTextColor(ContextCompat.getColor(this, R.color.peru));
                updateBtn.setTextSize(16);
                updateBtn.setTypeface(Typeface.DEFAULT_BOLD);
                cancelBtn.setTextColor(ContextCompat.getColor(this, R.color.peru));
                cancelBtn.setTextSize(16);
                cancelBtn.setTypeface(Typeface.DEFAULT_BOLD);
                // Handle update click
                updateBtn.setOnClickListener(v -> {
                    String newPassword = input.getText().toString().trim();
                    if (newPassword.length() < 6) {
                        input.setError("Password must be at least 6 characters");
                        return;
                    }
                    controller.updatePassword(newPassword, controller.getListener());
                    dialog.dismiss();
                });
                // Handle Cancel click
                cancelBtn.setOnClickListener(v -> {
                    dialog.dismiss(); // Close Change Password dialog
                    // Relaunch settings dialog from ProviderDashboardActivity
                    this.showSettingsDialog();
                });

            });

            dialog.show();

            // Auto-focus and show keyboard
            input.requestFocus();
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        private void openChangeProfileDialog() {
            Intent intent = new Intent(ProviderDashboardActivity.this, EditProfileActivity.class);
            startActivity(intent);
        }

        private void confirmDeleteAccount() {
            // 1. FIRST DIALOG – Use Android's "business" icon (ic_menu_manage)
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Stop Your Business?")
                    .setMessage("Do you really want to stop your business here?")
                    .setIcon(android.R.drawable.ic_menu_manage)  // Built-in icon
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes, stop", (d, w) -> showSecondConfirmation())
                    .setCancelable(false)
                    .show();
        }

        private void showSecondConfirmation() {
            // 2. SECOND DIALOG – with "Delete Forever" button
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Account Forever?")
                    .setMessage("This action is irreversible. Your account, services, and all data will be permanently deleted.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete Forever", (dialog, which) -> {
                        // DELETE DATA HERE
                        controller.deleteAccount(new ProviderController.DeleteAccountCallback() {
                            @Override public void onSuccess(String message) {
                                Toast.makeText(ProviderDashboardActivity.this, message, Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                                SessionManager.clearSession(ProviderDashboardActivity.this);
                                startActivity(new Intent(ProviderDashboardActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finishAffinity();
                            }
                            @Override public void onError(String error) {
                                Toast.makeText(ProviderDashboardActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setCancelable(false)
                    .show()
                    .getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

    }