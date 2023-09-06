package com.example.imagepickertest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);
        adView=findViewById(R.id.adView);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.uploadImageContainer, UploadImage.getInstance()).commit();
        }
        MobileAds.initialize(this);
        AdRequest adRequest=new AdRequest.Builder().build();
        adView.loadAd(adRequest);

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
            Fragment fragment = null;

            int itemId = item.getItemId();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (itemId == R.id.optHome) {

                fragmentTransaction.replace(R.id.uploadImageContainer, new EmptyFragment()).replace(R.id.newUpload, new EmptyFragment())
                        .replace(R.id.compressedImageFrame, new EmptyFragment())
                        .replace(R.id.uploadImageContainer, new UploadImage())
                        .commit();


            } else if (itemId == R.id.optAboutUs) {
                fragment = AboutUs.newInstance();

            } else if (itemId == R.id.optPrivacyPolicy) {
                fragment = PrivacyPolicy.newInstance();


            } else {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.linkedInUrl))));
            }
            if (fragment != null) {
                replaceFragment(fragment);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.uploadImageContainer, fragment)
                .commit();
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


