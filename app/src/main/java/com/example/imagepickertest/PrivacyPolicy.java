package com.example.imagepickertest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrivacyPolicy#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrivacyPolicy extends Fragment {


    public static PrivacyPolicy newInstance() {
        return new PrivacyPolicy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    }
}