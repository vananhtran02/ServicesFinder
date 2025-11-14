package edu.sjsu.android.servicesfinder.controller;

import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//*******************************************************************************************
// Focused on extracting data from UI components and formatting it for storage
// - Collecting data from UI components
// - Formatting data for storage
// - Converting UI selections to strings
//*******************************************************************************************
public class FormHelper {

    /**
     * Get selected availability days from checkboxes
     * @return Comma-separated string of days (e.g., "Mon, Tue, Fri")
     */
    public static String getSelectedAvailability(CheckBox mon, CheckBox tue, CheckBox wed,
                                                 CheckBox thu, CheckBox fri, CheckBox sat, CheckBox sun) {
        List<String> days = new ArrayList<>();
        if (mon.isChecked()) days.add("Mon");
        if (tue.isChecked()) days.add("Tue");
        if (wed.isChecked()) days.add("Wed");
        if (thu.isChecked()) days.add("Thu");
        if (fri.isChecked()) days.add("Fri");
        if (sat.isChecked()) days.add("Sat");
        if (sun.isChecked()) days.add("Sun");
        return String.join(", ", days);
    }

    /**
     * Format category selection map into readable string
     * @param selectedItems Map of category -> services
     * @return Formatted string (e.g., "Plumbing: Pipes, Toilets | Electrical: Wiring")
     */
    public static String formatCategoryFromSelection(Map<String, Set<String>> selectedItems) {
        List<String> summary = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : selectedItems.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                summary.add(entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
        }
        return String.join(" | ", summary);
    }

    /**
     * Get selected contact preference from RadioGroup
     * @param radioGroup RadioGroup containing contact options
     * @param activity Activity context to find RadioButton
     * @return Selected radio button text
     */
    public static String getSelectedContactPreference(RadioGroup radioGroup, android.app.Activity activity) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return "";
        RadioButton radioButton = activity.findViewById(selectedId);
        return radioButton != null ? radioButton.getText().toString() : "";
    }

    /**
     * Get text from TextInputEditText
     * @param editText TextInputEditText to get text from
     * @return Trimmed text or empty string
     */
    public static String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    /**
     * Get selected item from MaterialAutoCompleteTextView
     * @param autoCompleteTextView MaterialAutoCompleteTextView to get text from
     * @return Trimmed text or empty string
     */
    public static String getSelectedItem(MaterialAutoCompleteTextView autoCompleteTextView) {
        return autoCompleteTextView.getText() != null ? autoCompleteTextView.getText().toString().trim() : "";
    }
}