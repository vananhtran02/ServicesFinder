package edu.sjsu.android.servicesfinder.controller;

import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

//*****************************************************************
// UIHelper - for Spinner/Checkbox/Radio  in dashboard
// Restoring and Setting checkbox/radio/Spinner selections
//*****************************************************************
public class UIHelper {

    //************************************************************************************
    // Check box (Availability)
    //************************************************************************************
    public static void setAvailabilityCheckboxes(String availability,
                                                 CheckBox mon, CheckBox tue, CheckBox wed,
                                                 CheckBox thu, CheckBox fri, CheckBox sat, CheckBox sun) {
        if (availability == null) return;

        mon.setChecked(availability.contains("Mon"));
        tue.setChecked(availability.contains("Tue"));
        wed.setChecked(availability.contains("Wed"));
        thu.setChecked(availability.contains("Thu"));
        fri.setChecked(availability.contains("Fri"));
        sat.setChecked(availability.contains("Sat"));
        sun.setChecked(availability.contains("Sun"));
    }
    //************************************************************************************
    // Spinner (Service Area)
    //************************************************************************************
    public static void setSpinnerSelection(MaterialAutoCompleteTextView autoCompleteTextView, String value) {
        autoCompleteTextView.setText(value, false);  // 'false' prevents dropdown from showing
    }
    //************************************************************************************
    // Radio btn (Preferred Contact)
    //************************************************************************************
    public static void setRadioSelection(RadioGroup radioGroup, String text, android.app.Activity activity) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            android.view.View child = radioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (rb.getText().toString().equals(text)) {
                    rb.setChecked(true);
                    break;
                }
            }
        }
    }
}