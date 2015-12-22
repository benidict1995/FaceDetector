package com.example.benidictdulce.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private static final int TAKE_PICTURE_CODE = 100;
    private static final int MAX_FACES = 5;

    private Bitmap cameraBitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.take_picture)).setOnClickListener(btnClick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (TAKE_PICTURE_CODE == requestCode) {
            processCameraImage(data);
        }
    }

    private void OpenCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_CODE);
    }

    private void processCameraImage(Intent intent) {
        setContentView(R.layout.detectlayout);

        ((Button) findViewById(R.id.detect_face)).setOnClickListener(btnClick);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);

        cameraBitmap = (Bitmap) intent.getExtras().get("data");

        imageView.setImageBitmap(cameraBitmap);
    }


    private void detectFaces() {
        if (null != cameraBitmap) {
            int width = cameraBitmap.getWidth();
            int height = cameraBitmap.getHeight();

            FaceDetector detector = new FaceDetector(width, height, MainActivity.MAX_FACES);
            FaceDetector.Face[] faces = new FaceDetector.Face[MainActivity.MAX_FACES];
            Bitmap bitmap565 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Paint ditherPaint = new Paint();
            Paint drawPaint = new Paint();

            ditherPaint.setDither(true);
            drawPaint.setColor(Color.RED);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(2);

            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap565);
            canvas.drawBitmap(cameraBitmap, 0, 0, ditherPaint);


            int facesfound = detector.findFaces(bitmap565, faces);
            PointF midPoint = new PointF();
            float eyeDistance = 0.0f;
            float confidence = 0.0f;


            Log.i("Face to Face", "Bilang ng Mukhang Unggoy: " + facesfound);

            if (facesfound > 0) {
                for (int index = 0; index < facesfound; index++) {
                    faces[index].getMidPoint(midPoint);
                    eyeDistance = faces[index].eyesDistance();
                    confidence = faces[index].confidence();

                    Log.i("FaceDetector",
                            "Confidence: " + confidence +
                                    ", Eye distance: " + eyeDistance +
                                    ", Mid Point: (" + midPoint.x + ", " + midPoint.y + ")");

                    canvas.drawRect((int) midPoint.x - eyeDistance,
                            (int) midPoint.y - eyeDistance,
                            (int) midPoint.x + eyeDistance,
                            (int) midPoint.y + eyeDistance, drawPaint);
                }
            }

            String filepath = Environment.getExternalStorageDirectory() + "/facedetect" + System.currentTimeMillis() + ".jpg";

            try {
                FileOutputStream fos = new FileOutputStream(filepath);

                bitmap565.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView) findViewById(R.id.image_view);

            imageView.setImageBitmap(bitmap565);


        }
    }


    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    OpenCamera();
                    break;
                case R.id.detect_face:
                    detectFaces();
                    break;
            }
        }
    };

}
