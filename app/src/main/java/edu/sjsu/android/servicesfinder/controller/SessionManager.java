package edu.sjsu.android.servicesfinder.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public final class SessionManager {
    private static final String PREF = "services_finder_prefs";
    private static final String KEY_PROVIDER_ID = "provider_id";
    private static final String KEY_PROVIDER_NAME = "provider_name";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_CUSTOMER_NAME = "customer_name";
    private static final String KEY_USER_TYPE = "user_type"; // "provider" or "customer"

    public static final String USER_TYPE_PROVIDER = "provider";
    public static final String USER_TYPE_CUSTOMER = "customer";

    private SessionManager() {}

    // =========================================================
    // SAVE SESSION
    // =========================================================
    public static void saveProvider(Context ctx, String providerId, String providerName) {
        if (ctx == null || providerId == null || providerId.trim().isEmpty()) {
            return;
        }
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_PROVIDER_ID, providerId)
                .putString(KEY_PROVIDER_NAME, providerName == null ? "" : providerName)
                .putString(KEY_USER_TYPE, USER_TYPE_PROVIDER)
                .apply();
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public static String getProviderId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_PROVIDER_ID, null);
    }

    public static String getProviderName(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_PROVIDER_NAME, "");
    }

    // =========================================================
    // CLEAR SESSION
    // =========================================================
    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // Alias method for readability (used by UI / Controller)
    public static void clearSession(Context ctx) {
        clear(ctx);
    }

    // Optional: quick check to see if user is logged in
    public static boolean isLoggedIn(Context ctx) {
        return getProviderId(ctx) != null;
    }

    // =========================================================
    // CUSTOMER SESSION MANAGEMENT
    // =========================================================
    public static void saveCustomer(Context ctx, String customerId, String customerName) {
        if (ctx == null || customerId == null || customerId.trim().isEmpty()) {
            return;
        }
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_CUSTOMER_ID, customerId)
                .putString(KEY_CUSTOMER_NAME, customerName == null ? "" : customerName)
                .putString(KEY_USER_TYPE, USER_TYPE_CUSTOMER)
                .apply();
    }

    public static String getCustomerId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_CUSTOMER_ID, null);
    }

    public static String getCustomerName(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_CUSTOMER_NAME, "");
    }

    // =========================================================
    // USER TYPE MANAGEMENT
    // =========================================================
    public static String getUserType(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_USER_TYPE, null);
    }

    public static boolean isProvider(Context ctx) {
        return USER_TYPE_PROVIDER.equals(getUserType(ctx));
    }

    public static boolean isCustomer(Context ctx) {
        return USER_TYPE_CUSTOMER.equals(getUserType(ctx));
    }

    public static boolean isCustomerLoggedIn(Context ctx) {
        return getCustomerId(ctx) != null && isCustomer(ctx);
    }

    public static boolean isProviderLoggedIn(Context ctx) {
        return getProviderId(ctx) != null && isProvider(ctx);
    }
}
