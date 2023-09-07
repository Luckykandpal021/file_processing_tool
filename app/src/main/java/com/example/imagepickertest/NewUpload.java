package com.example.imagepickertest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewUpload extends Fragment {


    public static NewUpload getInstance() {
        return new NewUpload();
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_upload, container, false);
        rootView.findViewById(R.id.newUploadImageBtn).setOnClickListener(v -> {
            // Replace the current fragment with DisplayImageFragment and pass the URI
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.uploadImageContainer, new EmptyFragment()).replace(R.id.newUpload, new EmptyFragment())
                    .replace(R.id.compressedImageFrame,new EmptyFragment())
                    .replace(R.id.uploadImageContainer, new UploadImage())
                    .commit();

        });

        return rootView;
    }
}