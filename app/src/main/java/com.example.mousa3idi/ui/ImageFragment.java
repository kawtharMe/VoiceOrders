package com.example.mousa3idi.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;

import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mousa3idi.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;


import androidx.camera.lifecycle.ProcessCameraProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import androidx.fragment.app.Fragment;

import static android.content.Context.CAMERA_SERVICE;
import static android.graphics.BitmapFactory.decodeFile;

public class ImageFragment extends Fragment {

    private MyListener listener;

    private class YourAnalyzer implements ImageAnalysis.Analyzer {

        private int degreesToFirebaseRotation(int degrees) {
            switch (degrees) {
                case 0:
                    return FirebaseVisionImageMetadata.ROTATION_0;
                case 90:
                    return FirebaseVisionImageMetadata.ROTATION_90;
                case 180:
                    return FirebaseVisionImageMetadata.ROTATION_180;
                case 270:
                    return FirebaseVisionImageMetadata.ROTATION_270;
                default:
                    throw new IllegalArgumentException(
                            "Rotation must be 0, 90, 180, or 270.");
            }
        }

        @SuppressLint("UnsafeExperimentalUsageError")
        @Override
        public void analyze(ImageProxy imageProxy) {
            if (imageProxy == null || imageProxy.getImage() == null) {
                return;
            }
            Image mediaImage = imageProxy.getImage();
            int degrees =imageProxy.getImageInfo().getRotationDegrees();
            int rotation = degreesToFirebaseRotation(degrees);
            FirebaseVisionImage image =
                    FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(480)
                    .setHeight(360)
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(rotation)
                    .build();

            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                    .getOnDeviceImageLabeler();
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {

                            imageProxy.close();
                            int i=0;
                            String s="";
                            for (FirebaseVisionImageLabel label : labels) {
                                i++;
                                if(i==1)
                               s=s+" "+label.getText();
                                else if(i<3)
                                    s=s+" و "+label.getText();

                                Log.d("text",s);

                             Log.d("conf",String.valueOf(label.getConfidence()));
                            }
                            if(s.equals("")==false) {
                                listener.init();

                                Log.d("text", s);
                                if (s.equals("Home good"))
                                    listener.readMessage("اثاث", 1);
                                else if (s.equals("Fashion good"))
                                    listener.readMessage("اكسسوارات منزلية", 1);
                                else
                                    if(s.equals("Tableware"))
                                listener.readMessage("طاولة صغيرة", 1);
                               else
                                   if(s.equals("Musical instrument"))
                                       listener.readMessage("شاشة", 1);
                                   else
                                    listener.translate(s);
                            }else
                                listener.readMessage("يوجد خطأ ما الرجاء تكرار المحاولة",1);
                        }



                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            listener.readMessage("يوجد خطأ ما الرجاء تكرار المحاولة",1);
                        }
                    });
        }

        private  final SparseIntArray ORIENTATIONS = new SparseIntArray();
     {
            ORIENTATIONS.append(Surface.ROTATION_0, 90);
            ORIENTATIONS.append(Surface.ROTATION_90, 0);
            ORIENTATIONS.append(Surface.ROTATION_180, 270);
            ORIENTATIONS.append(Surface.ROTATION_270, 180);
        }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private int getRotationCompensation (String cameraId, Activity activity, Context context)
                throws CameraAccessException {

            int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int rotationCompensation = ORIENTATIONS.get(deviceRotation);


            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(CAMERA_SERVICE);
            int sensorOrientation = cameraManager
                    .getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;


            int result;
            switch (rotationCompensation) {
                case 0:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    break;
                case 90:
                    result = FirebaseVisionImageMetadata.ROTATION_90;
                    break;
                case 180:
                    result = FirebaseVisionImageMetadata.ROTATION_180;
                    break;
                case 270:
                    result = FirebaseVisionImageMetadata.ROTATION_270;
                    break;
                default:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    Log.e("TAG", "Bad rotation value: " + rotationCompensation);
            }
            return result;
        }

    }












        PreviewView prevView;
        private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

        private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};



        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.fragment_image, container, false);
            prevView = root.findViewById(R.id.texture);

            if (allPermissionsGranted()) {
                startCamera();
            } else {
                listener.readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);

            }
            return root;
        }

        private void startCamera() {

            cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());

            cameraProviderFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {

                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        cameraProvider.unbindAll();
                      //  bindPreview(cameraProvider);


                        Preview preview = new Preview.Builder()
                                .build();

                        CameraSelector cameraSelector = new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build();

                        preview.setSurfaceProvider(prevView.getSurfaceProvider());

                        ImageAnalysis imageAnalysis =
                                new ImageAnalysis.Builder()
                                        .setTargetResolution(new Size(1280, 720))
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build();
                        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getActivity()), new YourAnalyzer());

                        try {
                            Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) getActivity(), cameraSelector, preview, imageAnalysis);
                        }catch(Exception e)
                        {Log.d("errCam", e.toString());}

                        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) getActivity(), cameraSelector, preview, imageAnalysis);
                    } catch (ExecutionException | InterruptedException e) {

                        Log.d("errCam",e.toString());
                    }
                }
            }, ContextCompat.getMainExecutor(getActivity()));


        }

        void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        }


        private boolean allPermissionsGranted() {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }





    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (MyListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }


    public interface MyListener{
        void listen(int i);
        void readMessage(String s,int i);
        void init();
        void translate(String text);
    }

}