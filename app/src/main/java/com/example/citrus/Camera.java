package com.example.citrus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.example.citrus.ml.ModelUnquant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class Camera extends AppCompatActivity {

    ImageView imageView;
    CardView captureButton;
    CardView uploadButton; // New upload button
    ImageView iconCapture, iconUpload;
    CardView blackspot, canker, greening, other;
    int imageSize = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        captureButton = findViewById(R.id.cardview_capture);
        uploadButton = findViewById(R.id.cardview_upload); // Reference to upload button
        iconCapture = findViewById(R.id.icon_capture);
        iconUpload = findViewById(R.id.icon_upload);
        blackspot = findViewById(R.id.cardview_blackspot);
        canker = findViewById(R.id.cardview_canker);
        greening = findViewById(R.id.cardview_greening);
        other = findViewById(R.id.cardview_other);


        // Capture Button Functionality
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        // Upload Button Functionality
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery to select an image
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2); // Request code 2 for gallery
            }
        });

        // Set OnClickListener for the "black spot" CardView
        blackspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open UnhealthyActivity
                Intent intent = new Intent(Camera.this, blackspot.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "canker" CardView
        canker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open HealthyActivity
                Intent intent = new Intent(Camera.this, canker.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "greening" CardView
        greening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open HealthyActivity
                Intent intent = new Intent(Camera.this, greening.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for the "healthy" CardView
//        other.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Open HealthyActivity
//                Intent intent = new Intent(Camera.this, healthy.class);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bitmap image = null;
            if (requestCode == 1) {
                // Image captured from the camera
                image = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == 2) {
                // Image selected from the gallery
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (image != null) {
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Prepare the image buffer
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Run inference
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Process results
            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"blackspot", "canker", "greening", "other"};

            // Display result based on classification
            if ("blackspot".equals(classes[maxPos])) {
                blackspot.setVisibility(View.VISIBLE);
                canker.setVisibility(View.GONE);
                greening.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
            } else if ("canker".equals(classes[maxPos])) {
                canker.setVisibility(View.VISIBLE);
                blackspot.setVisibility(View.GONE);
                greening.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
            } else if ("greening".equals(classes[maxPos])) {
                greening.setVisibility(View.VISIBLE);
                blackspot.setVisibility(View.GONE);
                canker.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
            } else if ("other".equals(classes[maxPos])) {
                other.setVisibility(View.VISIBLE);
                blackspot.setVisibility(View.GONE);
                greening.setVisibility(View.GONE);
                canker.setVisibility(View.GONE);
            }

            else {
                blackspot.setVisibility(View.GONE);
                canker.setVisibility(View.GONE);
                greening.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
            }

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
