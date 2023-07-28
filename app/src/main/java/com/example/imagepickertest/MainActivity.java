package com.example.imagepickertest;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> mTakePhoto;
    private Button uploadBtn, compressBtn, downloadManage, fileLocation;
    private ImageView uploadedImg, compressedImg;
    private TextView testTxt, uploadedImage, compressedImageText, testTxting;
    private Uri resultData;
    private Bitmap compressedBitMap, uploadedImageBitmap;
    String savedImagePath ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uploadBtn = findViewById(R.id.upload);
        compressBtn = findViewById(R.id.compressBtn);
        uploadedImg = findViewById(R.id.imageView);
        testTxt = findViewById(R.id.testTxt);
        compressedImg = findViewById(R.id.compressImageView);
        compressedImageText = findViewById(R.id.compressedImage);
        downloadManage = findViewById(R.id.downloadManage);
        uploadedImage = findViewById(R.id.uploadedImage);
        fileLocation = findViewById(R.id.fileLocation);
        testTxting = findViewById(R.id.testTxting);
        mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        resultData = result;
                        uploadedImg.setImageURI(result);
                        uploadBtn.setVisibility(View.GONE);
                        compressBtn.setVisibility(View.VISIBLE);
                        String sizeImage = getImageSize(result);
                        uploadedImage.setVisibility(View.VISIBLE);
                        uploadedImage.setText(sizeImage);
                        Toast.makeText(this, "Image Size" + sizeImage, Toast.LENGTH_LONG).show();
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
    }



    @NonNull
    private String compressImageButton() {
        if (resultData != null) {
            testTxt.setText(resultData.toString());
            try {
                InputStream inputStream = getContentResolver().openInputStream(resultData);
                uploadedImageBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Compress the image
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                uploadedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
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
                compressBtn.setVisibility(View.GONE);
                downloadManage.setVisibility(View.VISIBLE);
                compressedImageText.setText("Compressed Image Size: " + compressedImageSizeText);
                compressedImageText.setVisibility(View.VISIBLE);
//                bitmapToUri(compressedBitMap);
                Toast.makeText(this, "Successfully Compressed", Toast.LENGTH_LONG).show();
                return compressedImageSizeText;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to compress the image", Toast.LENGTH_LONG).show();
            }
        }

        return "Unknown";
    }
//        savedImagePath


    private void saveImageToGallery() {
        if (compressedBitMap == null) {
            Toast.makeText(this, "Compressed image is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, "tretert.jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            uploadedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            outputStream.flush();
            outputStream.close();

            // Use FileProvider to get the content URI for the saved image
            Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);

            // Save the content URI as a string
            savedImagePath = contentUri.toString();

            Log.e("savedImagePath", savedImagePath);
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() throws IOException {
        if (savedImagePath != null) {
            Log.e("SAVEDIMAGEPATH", savedImagePath);

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
}

class Constants {
    public static final int IMAGE_DEFAULT_SIZE = 1000;
}
