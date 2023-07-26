package com.example.imagepickertest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> mTakePhoto;
    private Button uploadBtn, compressBtn, downloadManage;
    private ImageView uploadedImg, compressedImg;
    private TextView testTxt;
    private Uri resultData;
    private Bitmap compressedBitMap, uploadedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uploadBtn = findViewById(R.id.upload);
        compressBtn = findViewById(R.id.compressBtn);
        uploadedImg = findViewById(R.id.imageView);
        testTxt = findViewById(R.id.testTxt);
        compressedImg = findViewById(R.id.compressImageView);
        downloadManage = findViewById(R.id.downloadManage);
        mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        resultData = result;
                        uploadedImg.setImageURI(result);
                        uploadBtn.setVisibility(View.GONE);
                        compressBtn.setVisibility(View.VISIBLE);
                    }
                });

        uploadBtn.setOnClickListener(view -> mTakePhoto.launch("image/*"));

        compressBtn.setOnClickListener(view -> {
            if (resultData != null) {
                testTxt.setText(resultData.toString());
                try {
                    InputStream inputStream = getContentResolver().openInputStream(resultData);
                    uploadedImageBitmap = BitmapFactory.decodeStream(inputStream);
                    compressImage(); // Compress the image only once
                    compressedImg.setImageBitmap(compressedBitMap);
                    compressBtn.setVisibility(View.GONE);
                    downloadManage.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Successfully Compressed", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to compress the image", Toast.LENGTH_LONG).show();
                }
            }
        });

        downloadManage.setOnClickListener(view -> saveImageToGallery());
    }

    private void compressImage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        uploadedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50 , outputStream);
        byte[] compressedImageData = outputStream.toByteArray();
        outputStream.close();

        compressedBitMap = BitmapFactory.decodeByteArray(compressedImageData, 0, compressedImageData.length);
    }

    private void saveImageToGallery() {
        if (compressedBitMap == null) {
            Toast.makeText(this, "Compressed image is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, "newImg12253.jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            compressedBitMap.compress(Bitmap.CompressFormat.JPEG, 50 , outputStream);
            outputStream.flush();
            outputStream.close();
            Intent mediaScanNet = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanNet.setData(Uri.fromFile(imageFile));
            sendBroadcast(mediaScanNet);
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show();
        }
    }
}
