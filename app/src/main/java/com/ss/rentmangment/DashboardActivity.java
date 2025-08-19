package com.ss.rentmangment;

import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.LinearLayout;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    // Navigation components
    private LinearLayout navHome, navRooms, navTenants, navSettings;
    private ImageView iconHome, iconRooms, iconTenants, iconSettings;
    private TextView textHome, textRooms, textTenants, textSettings;

    private LinearLayout currentSelectedNav;

    // Animation constants
    private static final int ANIMATION_DURATION = 250;
    private static final float SELECTED_SCALE = 1.15f;
    private static final float NORMAL_SCALE = 1.0f;
    private static final float TEXT_BOUNCE_HEIGHT = -6f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Log.d(TAG, "onCreate: Setting up navigation");

        initializeViews();
        setupClickListeners();

        // Load default fragment and set initial selection
        if (savedInstanceState == null) {
            Log.d(TAG, "Loading initial HomeFragment");
            loadFragment(new HomeFragment());
            setSelected(navHome, iconHome, textHome);
        }
    }

    private void initializeViews() {
        // Initialize LinearLayout containers
        navHome = findViewById(R.id.nav_home);
        navRooms = findViewById(R.id.nav_rooms);
        navTenants = findViewById(R.id.nav_tenants);
        navSettings = findViewById(R.id.nav_settings);

        // Initialize ImageViews
        iconHome = findViewById(R.id.icon_home);
        iconRooms = findViewById(R.id.icon_rooms);
        iconTenants = findViewById(R.id.icon_tenants);
        iconSettings = findViewById(R.id.icon_settings);

        // Initialize TextViews
        textHome = findViewById(R.id.text_home);
        textRooms = findViewById(R.id.text_rooms);
        textTenants = findViewById(R.id.text_tenants);
        textSettings = findViewById(R.id.text_settings);

        // Debug: Check if views are found
        Log.d(TAG, "navHome: " + (navHome != null ? "Found" : "NULL"));
        Log.d(TAG, "navRooms: " + (navRooms != null ? "Found" : "NULL"));
        Log.d(TAG, "navTenants: " + (navTenants != null ? "Found" : "NULL"));
        Log.d(TAG, "navSettings: " + (navSettings != null ? "Found" : "NULL"));
    }

    private void setupClickListeners() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Log.d(TAG, "Home clicked");
                setSelected(navHome, iconHome, textHome);
                loadFragment(new HomeFragment());
            });
        }

        if (navRooms != null) {
            navRooms.setOnClickListener(v -> {
                Log.d(TAG, "Rooms clicked");
                setSelected(navRooms, iconRooms, textRooms);
                loadFragment(new RoomsFragment());
            });
        }

        if (navTenants != null) {
            navTenants.setOnClickListener(v -> {
                Log.d(TAG, "Tenants clicked");
                setSelected(navTenants, iconTenants, textTenants);
                loadFragment(new TenantsFragment());
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings clicked");
                setSelected(navSettings, iconSettings, textSettings);
                loadFragment(new SettingsFragment());
            });
        }
    }

    // FIXED: Simplified selection with working animations
    private void setSelected(LinearLayout navItem, ImageView icon, TextView text) {
        Log.d(TAG, "Setting selected: " + (navItem == navHome ? "Home" :
                navItem == navRooms ? "Rooms" :
                        navItem == navTenants ? "Tenants" : "Settings"));

        // Reset ALL items first
        resetAllNavItemsComplete();

        // Set current as selected
        currentSelectedNav = navItem;

        // Animate selected item
        if (icon != null && text != null) {
            // Icon animations
            icon.animate()
                    .scaleX(SELECTED_SCALE)
                    .scaleY(SELECTED_SCALE)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();

            // Icon color change - USING CUSTOM COLOR
            icon.setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.SRC_IN);

            // Text animations
            text.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();

            // Text color change - USING CUSTOM COLOR
            text.setTextColor(getResources().getColor(R.color.purple_700));

            // Text bounce effect
            text.animate()
                    .translationY(TEXT_BOUNCE_HEIGHT)
                    .setDuration(ANIMATION_DURATION / 2)
                    .withEndAction(() -> {
                        text.animate()
                                .translationY(0f)
                                .setDuration(ANIMATION_DURATION / 2)
                                .setInterpolator(new BounceInterpolator())
                                .start();
                    })
                    .start();
        }
    }

    // FIXED: Complete reset of all items
    private void resetAllNavItemsComplete() {
        // Reset Home
        if (iconHome != null && textHome != null) {
            iconHome.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).setDuration(ANIMATION_DURATION / 2).start();
            iconHome.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
            textHome.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).translationY(0f).setDuration(ANIMATION_DURATION / 2).start();
            textHome.setTextColor(getResources().getColor(android.R.color.black));
        }

        // Reset Rooms
        if (iconRooms != null && textRooms != null) {
            iconRooms.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).setDuration(ANIMATION_DURATION / 2).start();
            iconRooms.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
            textRooms.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).translationY(0f).setDuration(ANIMATION_DURATION / 2).start();
            textRooms.setTextColor(getResources().getColor(android.R.color.black));
        }

        // Reset Tenants
        if (iconTenants != null && textTenants != null) {
            iconTenants.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).setDuration(ANIMATION_DURATION / 2).start();
            iconTenants.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
            textTenants.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).translationY(0f).setDuration(ANIMATION_DURATION / 2).start();
            textTenants.setTextColor(getResources().getColor(android.R.color.black));
        }

        // Reset Settings
        if (iconSettings != null && textSettings != null) {
            iconSettings.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).setDuration(ANIMATION_DURATION / 2).start();
            iconSettings.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
            textSettings.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).translationY(0f).setDuration(ANIMATION_DURATION / 2).start();
            textSettings.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    // FIXED: Simpler fragment loading
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            Log.d(TAG, "Loading fragment: " + fragment.getClass().getSimpleName());

            try {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Use simple animations that always work
                transaction.setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );

                transaction.replace(R.id.fragment_container, fragment);
                transaction.commit();

                Log.d(TAG, "Fragment loaded successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error loading fragment", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Handle back button to go to home fragment
        if (currentSelectedNav != navHome) {
            setSelected(navHome, iconHome, textHome);
            loadFragment(new HomeFragment());
        } else {
            super.onBackPressed();
        }
    }
}
