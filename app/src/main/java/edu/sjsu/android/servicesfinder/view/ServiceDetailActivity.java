package edu.sjsu.android.servicesfinder.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import edu.sjsu.android.servicesfinder.R;

/**
 * ServiceDetailActivity - Shows complete service details with contact options
 */
public class ServiceDetailActivity extends AppCompatActivity {

    // Service info
    private String serviceTitle;
    private String serviceDescription;
    private String servicePricing;
    private String serviceCategory;
    private String serviceArea;
    private String serviceAvailability;
    private String serviceImageUrl;

    // Provider info
    private String providerName;
    private String providerPhone;
    private String providerEmail;
    private String providerAddress;

    // UI Components
    private ImageView serviceImageView;
    private TextView serviceTitleText;
    private TextView servicePricingText;
    private TextView serviceDescriptionText;
    private TextView serviceCategoryText;
    private TextView serviceAreaText;
    private TextView serviceAvailabilityText;
    private TextView providerNameText;
    private TextView providerContactText;
    private Button callButton;
    private Button emailButton;
    private Button locationButton;
    /*
    Entry point of the Activity. Initializes the layout, enables the back button in the ActionBar,
 * sets the title, and triggers the sequence of data extraction, view initialization, data binding, and action setup.
 *


     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);               // Initializes the layout

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);      // Enable back button in the ActionBar
            getSupportActionBar().setTitle("Service Details");
        }

        getIntentExtras();                                              // Retrieves data passed from the main activity
        initializeViews();
        displayServiceInfo();
        setupActionButtons();
    }

    /** Retrieves service and provider data passed from previous activity */
    private void getIntentExtras() {
        Intent intent = getIntent();

        // Service data
        serviceTitle = intent.getStringExtra("serviceTitle");
        serviceDescription = intent.getStringExtra("serviceDescription");
        servicePricing = intent.getStringExtra("servicePricing");
        serviceCategory = intent.getStringExtra("serviceCategory");
        serviceArea = intent.getStringExtra("serviceArea");
        serviceAvailability = intent.getStringExtra("serviceAvailability");
        serviceImageUrl = intent.getStringExtra("serviceImageUrl");

        // Provider data
        providerName = intent.getStringExtra("providerName");
        providerPhone = intent.getStringExtra("providerPhone");
        providerEmail = intent.getStringExtra("providerEmail");
        providerAddress = intent.getStringExtra("providerAddress");
    }

    /** Initializes all UI view references */
    private void initializeViews() {
        serviceImageView = findViewById(R.id.serviceDetailImage);
        serviceTitleText = findViewById(R.id.serviceDetailTitle);
        servicePricingText = findViewById(R.id.serviceDetailPricing);
        serviceDescriptionText = findViewById(R.id.serviceDetailDescription);
        serviceCategoryText = findViewById(R.id.serviceDetailCategory);
        serviceAreaText = findViewById(R.id.serviceDetailArea);
        serviceAvailabilityText = findViewById(R.id.serviceDetailAvailability);
        providerNameText = findViewById(R.id.providerDetailName);
        providerContactText = findViewById(R.id.providerDetailContact);
        callButton = findViewById(R.id.callButton);
        emailButton = findViewById(R.id.emailButton);
        locationButton = findViewById(R.id.locationButton);
    }

    /** Displays all service and provider info in UI */
    private void displayServiceInfo() {
        // Title
        serviceTitleText.setText(serviceTitle);

        // Pricing
        if (servicePricing != null && !servicePricing.isEmpty()) {
            servicePricingText.setText(servicePricing);
            servicePricingText.setVisibility(View.VISIBLE);
        } else {
            servicePricingText.setVisibility(View.GONE);
        }

        // Description
        if (serviceDescription != null && !serviceDescription.isEmpty()) {
            serviceDescriptionText.setText(serviceDescription);
        } else {
            serviceDescriptionText.setText("No description available");
        }

        // Category
        if (serviceCategory != null && !serviceCategory.isEmpty()) {
            serviceCategoryText.setText("📂 " + serviceCategory);
        } else {
            serviceCategoryText.setVisibility(View.GONE);
        }

        // Area
        if (serviceArea != null && !serviceArea.isEmpty()) {
            serviceAreaText.setText("📍 Service Area: " + serviceArea);
        } else {
            serviceAreaText.setVisibility(View.GONE);
        }

        // Availability
        if (serviceAvailability != null && !serviceAvailability.isEmpty()) {
            serviceAvailabilityText.setText("📅 Available: " + serviceAvailability);
        } else {
            serviceAvailabilityText.setVisibility(View.GONE);
        }

        // Provider name
        providerNameText.setText("Provider: " + providerName);

        // Provider contact
        StringBuilder contactInfo = new StringBuilder();
        if (providerPhone != null && !providerPhone.isEmpty()) {
            contactInfo.append("📞 ").append(formatPhone(providerPhone));
        }
        if (providerEmail != null && !providerEmail.isEmpty()) {
            if (contactInfo.length() > 0) contactInfo.append("\n");
            contactInfo.append("✉️ ").append(providerEmail);
        }
        if (providerAddress != null && !providerAddress.isEmpty()) {
            if (contactInfo.length() > 0) contactInfo.append("\n");
            contactInfo.append("📍 ").append(providerAddress);
        }
        providerContactText.setText(contactInfo.toString());

        // Service image
        if (serviceImageUrl != null && !serviceImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(serviceImageUrl)
                    .placeholder(R.drawable.ic_service_placeholder)
                    .error(R.drawable.ic_service_placeholder)
                    .centerCrop()
                    .into(serviceImageView);
        } else {
            serviceImageView.setImageResource(R.drawable.ic_service_placeholder);
        }
    }

    /** Sets up call, email, and map buttons */
    private void setupActionButtons() {
        // Call
        callButton.setOnClickListener(v -> {
            if (providerPhone != null && !providerPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + providerPhone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Email
        emailButton.setOnClickListener(v -> {
            if (providerEmail != null && !providerEmail.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + providerEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about " + serviceTitle);
                intent.putExtra(Intent.EXTRA_TEXT, "Hi " + providerName + ",\n\nI'm interested in your service: " + serviceTitle + "\n\nCould you please provide more information?\n\nThank you!");

                try {
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Email not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Location
        locationButton.setOnClickListener(v -> {
            if (providerAddress != null && !providerAddress.isEmpty()) {
                Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(providerAddress));
                Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
                intent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(providerAddress)));
                    startActivity(browserIntent);
                }
            } else {
                Toast.makeText(this, "Address not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide or disable buttons when missing data
        if (providerPhone == null || providerPhone.isEmpty()) {
            callButton.setVisibility(View.GONE); // completely hide
        } else {
            callButton.setVisibility(View.VISIBLE);
        }

        if (providerEmail == null || providerEmail.isEmpty()) {
            emailButton.setVisibility(View.GONE); // hide email button
        } else {
            emailButton.setVisibility(View.VISIBLE);
        }

        if (providerAddress == null || providerAddress.isEmpty()) {
            locationButton.setVisibility(View.GONE);
        } else {
            locationButton.setVisibility(View.VISIBLE);
        }

    }

    /** Formats a 10-digit phone number as (XXX) XXX-XXXX */
    private String formatPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6));
        }
        return phone;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
