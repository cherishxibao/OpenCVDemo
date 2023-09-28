package com.demo.myopencv;

//import androidx.appcompat.app.AppCompatActivity;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import org.opencv.android.OpenCVLoader;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        initOpenCV();
//    }
//
//    private void initOpenCV() {
//        if(OpenCVLoader.initDebug()){
//            Toast.makeText(this, "opencv succ", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "opencv fail", Toast.LENGTH_SHORT).show();
//        }
//    }
//}

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE1 = 1;
    private static final int REQUEST_IMAGE2 = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Uri imageUri1;
    private Uri imageUri2;
    private TextView tvSimilarity;

    private ImageView imageView1;
    private ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSimilarity = findViewById(R.id.tvSimilarity);
        Button btnChooseImage1 = findViewById(R.id.btnChooseImage1);
        Button btnChooseImage2 = findViewById(R.id.btnChooseImage2);

        imageView1 = findViewById(R.id.chooseImage1);
        imageView2 = findViewById(R.id.chooseImage2);

        btnChooseImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(REQUEST_IMAGE1);
            }
        });

        btnChooseImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(REQUEST_IMAGE2);
            }
        });

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed.");
        }
    }

    private void chooseImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (requestCode == REQUEST_IMAGE1) {
                    imageUri1 = selectedImage;
                    imageView1.setImageURI(selectedImage);
                } else if (requestCode == REQUEST_IMAGE2) {
                    imageUri2 = selectedImage;
                    imageView2.setImageURI(selectedImage);
                }

                if (imageUri1 != null && imageUri2 != null) {
                    compareImages();
                }
            }
        }
    }

    private void compareImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri1);
            Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri2);

            Mat mat1 = new Mat(bitmap1.getHeight(), bitmap1.getWidth(), CvType.CV_8UC3);
            Mat mat2 = new Mat(bitmap2.getHeight(), bitmap2.getWidth(), CvType.CV_8UC3);

            Utils.bitmapToMat(bitmap1, mat1);
            Utils.bitmapToMat(bitmap2, mat2);

            Mat grayMat1 = new Mat();
            Mat grayMat2 = new Mat();

            Imgproc.cvtColor(mat1, grayMat1, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(mat2, grayMat2, Imgproc.COLOR_BGR2GRAY);

            double similarity = compareImagesSSIM(grayMat1, grayMat2);
            tvSimilarity.setText("Similarity: " + similarity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double compareImagesSSIM(Mat image1, Mat image2) {
        Mat ssimMap = new Mat();
        Imgproc.cvtColor(image2, image2, Imgproc.COLOR_GRAY2BGR);
        Imgproc.cvtColor(image1, image1, Imgproc.COLOR_GRAY2BGR);

        Imgproc.resize(image1, image1, new Size(image2.cols(), image2.rows()));

        Imgproc.cvtColor(image1, image1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(image2, image2, Imgproc.COLOR_BGR2GRAY);

        Imgproc.matchTemplate(image1, image2, ssimMap, Imgproc.TM_CCOEFF_NORMED);

        double[] result = new double[]{Core.minMaxLoc(ssimMap).maxVal};
        return result[0];
    }
}
