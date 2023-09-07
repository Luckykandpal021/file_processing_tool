package com.example.imagepickertest;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;


public class UploadImage extends Fragment {

    public static UploadImage getInstance() {
        return new UploadImage();
    }

    private ActivityResultLauncher<String> imagePickerLauncher;

    private String getSelectedImageSize(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();

        try {
            InputStream inputStream = contentResolver.openInputStream(uri);

            if (inputStream != null) {
                long uploadedImageSize = inputStream.available();

                // Convert the size to KB, MB, etc. with decimal precision
                String[] units = {"B", "KB", "MB", "GB"};
                int unitIndex = 0;
                double size = uploadedImageSize;

                while (size > 1024 && unitIndex < units.length - 1) {
                    size /= 1024;
                    unitIndex++;
                }
                inputStream.close();

                return String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_upload_image, container, false);
        // Find the button in the inflated layout
        Button uploadBtn = rootView.findViewById(R.id.uploadImageBtn);


        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {

                String selectedImageSize = getSelectedImageSize(uri);


                // Replace the current fragment with DisplayImageFragment and pass the URI
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.uploadImageContainer, SelectedImage.getInstance(uri.toString(), selectedImageSize)).replace(R.id.newUpload, NewUpload.getInstance());

                fragmentTransaction.commit();
            }
        });

        uploadBtn.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });


        return rootView;
    }
}