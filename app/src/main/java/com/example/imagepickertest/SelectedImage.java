package com.example.imagepickertest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class SelectedImage extends Fragment {

    private static final String ARG_PARAM1 = "selectedImage";
    private static final String ARG_PARAM2 = "selectedImageSize";

    private static int selectedQualityValue = 50;
    private static String imageUri, compressedImageSize1, fileName;
    private static Bitmap compressedBitmap;
    private static Uri testuri;


    private void compressImage(Uri imageUri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            if (inputStream != null) {
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);


                // Create an output stream to save the compressed image
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // Compress the image to the output stream
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, selectedQualityValue, outputStream);


                // Create a byte array from the compressed image
                byte[] compressedImageData = outputStream.toByteArray();
                compressedBitmap = BitmapFactory.decodeByteArray(compressedImageData, 0, compressedImageData.length);

                String[] units = {"B", "KB", "MB", "GB"};
                int unitIndex = 0;
                double size = compressedImageData.length;

                while (size > 1024 && unitIndex < units.length - 1) {
                    size /= 1024;
                    unitIndex++;
                }
                compressedImageSize1 = String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex]);
                originalBitmap.recycle();
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static SelectedImage getInstance(String pickImageUri, String selectedImageSize) {
        SelectedImage fragment = new SelectedImage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, pickImageUri);
        args.putString(ARG_PARAM2, selectedImageSize);
        fragment.setArguments(args);
        return fragment;
    }

    private void openGallery(Uri savedImagePath, String filename) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, savedImagePath);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, filename); // Set the filename as the subject
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    private void saveImageToGallery(Bitmap compressedBitmap, int quality) {
        if (compressedBitmap == null) {
            Toast.makeText(getContext(), "Compressed image is empty", Toast.LENGTH_SHORT).show();
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            fileName = "IMG_" + timeStamp + ".jpg";

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.WIDTH, compressedBitmap.getWidth());
            contentValues.put(MediaStore.Images.Media.HEIGHT, compressedBitmap.getHeight());
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image saved using MediaStore API");

            ContentResolver contentResolver = requireContext().getContentResolver();
            Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            testuri = imageUri;
            try {
                if (imageUri != null) {
                    // Open an output stream to write the bitmap data to the content URI
                    OutputStream outputStream = contentResolver.openOutputStream(imageUri);

                    if (outputStream != null) {
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream); // Compress and save the image

                        outputStream.close(); // Close the output stream

                        // Notify the MediaScanner about the new image
                        MediaScannerConnection.scanFile(getContext(),
                                new String[]{imageUri.getPath()},
                                new String[]{"image/jpeg"},
                                null);

                        Log.e("savedImagePath", imageUri.toString());

                        Toast.makeText(getContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show();


                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to save the image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selected_image, container, false);

        // Selected Image Attributes and Elements
        ImageView imageView = rootView.findViewById(R.id.selectedImage);
        Slider qualitySlider = rootView.findViewById(R.id.qualityRange);
        TextView selectedImageSizeTextView = rootView.findViewById(R.id.selectedImageSize);
        Button compressBtn = rootView.findViewById(R.id.compressBtn);
        TextView selectedQualityText = rootView.findViewById(R.id.selectedQualityValue);


//Compressed Image Attributes and Elements
        ImageView compressImageView = rootView.findViewById(R.id.compressedImageView);
        TextView compressedImageSize = rootView.findViewById(R.id.compressedImageSize);
        Button shareImageBtn = rootView.findViewById(R.id.shareCompressedBtn);
        Button saveCompressImage = rootView.findViewById(R.id.saveCompressImage);
        CardView compressedImageCard = rootView.findViewById(R.id.compressedImageCard);
//

        // Get the arguments that were passed to the fragment
        Bundle args = getArguments();
        if (args != null) {
            imageUri = args.getString(ARG_PARAM1);
            String selectedImageSize = getString(R.string.selected_image_size) + args.getString(ARG_PARAM2);
            selectedImageSizeTextView.setText(selectedImageSize);
            imageView.setImageURI(Uri.parse(imageUri));
        }
        qualitySlider.addOnChangeListener((slider, value, fromUser) -> {

            selectedQualityValue = (int) value;
            String selectedQualityTextValue = getString(R.string.selected_quality) + selectedQualityValue + "%";
            selectedQualityText.setText(selectedQualityTextValue);
        });

        compressBtn.setOnClickListener(v -> {

            compressImage(Uri.parse(imageUri));
            if (compressedBitmap != null) {
                compressedImageCard.setVisibility(View.VISIBLE);
                compressImageView.setImageBitmap(compressedBitmap);
                compressedImageSize.setText(compressedImageSize1);
                Toast.makeText(getContext(), "Image Compressed", Toast.LENGTH_SHORT).show();

            }
        });


        shareImageBtn.setOnClickListener(v -> {
            if (getContext() != null && compressedBitmap != null && testuri != null) {
                openGallery(testuri, fileName); // Pass the stored content URI and filename
            } else {
                Toast.makeText(getContext(), "Image is null", Toast.LENGTH_SHORT).show();
            }
        });

        saveCompressImage.setOnClickListener(v -> {
            if (compressedBitmap != null) {
                saveImageToGallery(compressedBitmap, selectedQualityValue);
                shareImageBtn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "Image is null", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

}