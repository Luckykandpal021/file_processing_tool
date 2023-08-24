package com.example.imagepickertest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    Button uploadBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadBtn = findViewById(R.id.uploadImageBtn);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.uploadImageContainer, UploadImage.getInstance()).commit();

        }


        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        toggle.setDrawerIndicatorEnabled(false); // Disable default toggle icon
        toggle.setHomeAsUpIndicator(R.drawable.baseline_menu_24); // Set your custom home icon
        toggle.setToolbarNavigationClickListener(v -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);


        navigationView.setNavigationItemSelectedListener(item -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (item.getItemId() == R.id.optHome) {

                // Replace the current fragment with DisplayImageFragment and pass the URI
                fragmentTransaction.replace(R.id.uploadImageContainer, new EmptyFragment()).replace(R.id.newUpload, new EmptyFragment())
                        .replace(R.id.compressedImageFrame, new EmptyFragment())
                        .replace(R.id.uploadImageContainer, new UploadImage())
                        .commit();


                ToastMessage("Home Click");
            } else if (item.getItemId() == R.id.optAboutUs) {
                fragmentTransaction.replace(R.id.uploadImageContainer, AboutUs.newInstance()).commit();

                ToastMessage("About Us Click");
            } else if (item.getItemId() == R.id.optPrivacyPolicy) {
                fragmentTransaction.replace(R.id.uploadImageContainer, PrivacyPolicy.newInstance()).commit();

                ToastMessage("Privacy Policy Click");

            } else {
                fragmentTransaction.replace(R.id.uploadImageContainer, DeveloperContact.newInstance()).commit();

                ToastMessage("Developer Option Click");

            }


            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    private void ToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}


