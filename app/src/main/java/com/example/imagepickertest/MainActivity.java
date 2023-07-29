package com.example.imagepickertest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> mTakePhoto;
    private Button uploadBtn, compressBtn, downloadManage, fileLocation, ReUpload;
    private ImageView uploadedImg, compressedImg;
    private TextView  uploadedImage, compressedImageText;
    private Uri resultData;
    private Bitmap compressedBitMap, uploadedImageBitmap;
    String savedImagePath;
    int qualityInput;
    private SeekBar qualityRange;
    private TextView textViewProgress;
    private FrameLayout bgUploadImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uploadBtn = findViewById(R.id.upload);
        compressBtn = findViewById(R.id.compressBtn);
        uploadedImg = findViewById(R.id.imageView);
        compressedImg = findViewById(R.id.compressImageView);
        compressedImageText = findViewById(R.id.compressedImage);
        downloadManage = findViewById(R.id.downloadManage);
        uploadedImage = findViewById(R.id.uploadedImage);
        fileLocation = findViewById(R.id.fileLocation);
        ReUpload = findViewById(R.id.ReUpload);
        bgUploadImage=findViewById(R.id.bgUploadImage);
         qualityRange = findViewById(R.id.qualityRange);
        textViewProgress = findViewById(R.id.textViewProgress);

        qualityRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qualityInput=progress;
                String qualityProgress="Progress: " + qualityInput;
                textViewProgress.setText(qualityProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used in this example
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used in this example
            }
        });
        mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        resultData = result;
                        uploadedImg.setVisibility(View.VISIBLE);
                        uploadedImg.setImageURI(result);
                        uploadBtn.setVisibility(View.GONE);
                        compressBtn.setVisibility(View.VISIBLE);
                        String sizeImage = getImageSize(result);
                        uploadedImage.setVisibility(View.VISIBLE);
                        String uploadedImageSize="Uploaded Image Size:- "+sizeImage;
                        uploadedImage.setText(uploadedImageSize);
                        bgUploadImage.setVisibility(View.GONE);
                        qualityRange.setVisibility(View.VISIBLE);
                        ReUpload.setVisibility(View.VISIBLE);
                        textViewProgress.setVisibility(View.VISIBLE);
                    }
                });
        uploadBtn.setOnClickListener(view -> mTakePhoto.launch("image/*"));

        compressBtn.setOnClickListener(view -> compressImageButton());

        downloadManage.setOnClickListener(view -> saveImageToGallery());
        fileLocation.setOnClickListener(view -> {
            try {
                openGallery();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ReUpload.setOnClickListener(v -> reUploadImage());
    }


    private void compressImageButton() {
        if (resultData != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(resultData);
                uploadedImageBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Compress the image
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                uploadedImageBitmap.compress(Bitmap.CompressFormat.JPEG, qualityInput, outputStream);
                byte[] compressedImageData = outputStream.toByteArray();
                outputStream.close();

                // Calculate the size of the compressed image in bytes
                long compressedImageSize = compressedImageData.length;

                // Convert the size to KB or MB for readability
                String[] units = {"B", "KB", "MB", "GB"};
                int unitIndex = 0;
                while (compressedImageSize > Constants.IMAGE_DEFAULT_SIZE && unitIndex < units.length - 1) {
                    compressedImageSize /= Constants.IMAGE_DEFAULT_SIZE;
                    unitIndex++;
                }
                compressedBitMap = BitmapFactory.decodeByteArray(compressedImageData, 0, compressedImageData.length);
                String compressedImageSizeText = compressedImageSize + " " + units[unitIndex];

                compressedImg.setImageBitmap(compressedBitMap);
                compressedImg.setVisibility(View.VISIBLE);
                downloadManage.setVisibility(View.VISIBLE);
                String compressedSize="Compressed Image Size: " + compressedImageSizeText;
                compressedImageText.setText(compressedSize);
                compressedImageText.setVisibility(View.VISIBLE);
//                bitmapToUri(compressedBitMap);
                Toast.makeText(this, "Image Successfully Compressed", Toast.LENGTH_SHORT).show();
                ReUpload.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to compress the image", Toast.LENGTH_SHORT).show();
            }
        }

    }

//        savedImagePath

    private void saveImageToGallery() {
        try {
            if (compressedBitMap == null) {
                Toast.makeText(this, "Compressed image is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";

            // Define the content values for the image
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.WIDTH, uploadedImageBitmap.getWidth());
            contentValues.put(MediaStore.Images.Media.HEIGHT, uploadedImageBitmap.getHeight());
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image saved using MediaStore API");

            // Insert the image into the MediaStore and get the content URI
            ContentResolver contentResolver = getContentResolver();
            Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                // Open an output stream to write the bitmap data to the content URI
                OutputStream outputStream = contentResolver.openOutputStream(imageUri);

                // Compress the bitmap to JPEG format and write it to the output stream
                uploadedImageBitmap.compress(Bitmap.CompressFormat.JPEG, qualityInput, outputStream);

                // Close the output stream
                outputStream.close();

                // Notify the MediaScanner about the new image so that it appears in the gallery
                MediaScannerConnection.scanFile(this,
                        new String[]{imageUri.getPath()},
                        new String[]{"image/jpeg"},
                        null);

                // Save the content URI as a string in savedImagePath variable
                savedImagePath = imageUri.toString();

                Log.e("savedImagePath", savedImagePath);
                compressBtn.setVisibility(View.GONE);
                Toast.makeText(this, "Image Saved in Gallery Picture Folder", Toast.LENGTH_SHORT).show();
                fileLocation.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to save image" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() throws IOException {
        if (savedImagePath != null) {

            // Convert the savedImagePath back to a Uri
            Uri contentUri = Uri.parse(savedImagePath);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*"); // Set the MIME type to share images
            sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "shareTitle");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }

    @NonNull
    private String getImageSize(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();

        try {
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            if (inputStream != null) {
                long uploadedImageSize = inputStream.available();

                // Convert the size to KB or MB for readability
                String[] units = {"B", "KB", "MB", "GB"};
                int unitIndex = 0;
                while (uploadedImageSize > Constants.IMAGE_DEFAULT_SIZE) {
                    uploadedImageSize /= Constants.IMAGE_DEFAULT_SIZE;
                    unitIndex++;
                }
                inputStream.close();

                return uploadedImageSize + " " + units[unitIndex];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void reUploadImage() {
        // Clear the resultData, compressedBitMap, and savedImagePath
        resultData = null;
        compressedBitMap = null;
        savedImagePath = null;

        // Clear the image views
        uploadedImg.setImageURI(null);

        // Set the compressedImg ImageView to an empty or transparent drawable
        compressedImg.setImageDrawable(null);

        // Hide the compressed image text and download manage button
        compressedImageText.setVisibility(View.GONE);
        downloadManage.setVisibility(View.GONE);

        // Clear the text in compressedImageText
        compressedImageText.setText(""); // Add this line
        bgUploadImage.setVisibility(View.VISIBLE);
        // Show the upload button again
        uploadBtn.setVisibility(View.VISIBLE);

        // Reset the text fields
        uploadedImage.setText("");
        ReUpload.setVisibility(View.GONE);
        fileLocation.setVisibility(View.GONE);
        compressedImg.setVisibility(View.GONE);
        uploadedImg.setVisibility(View.GONE);
        qualityRange.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.GONE);
    }

}

class Constants {
    public static final int IMAGE_DEFAULT_SIZE = 1000;
}
