package com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.CameraImageGraphic;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.FrameMetadata;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

public class BarcodeScanningProcessor extends VisionProcessorBase<List<FirebaseVisionBarcode>> {
    private static final String TAG = "BarcodeScanProc";
    private final FirebaseVisionBarcodeDetector detector;

    BarcodeResultListener barcodeResultListener;

    public BarcodeScanningProcessor() {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
    }

    public BarcodeScanningProcessor(FirebaseVisionBarcodeDetector detector) {
        this.detector = detector;
    }

    public BarcodeResultListener getBarcodeResultListener() {
        return barcodeResultListener;
    }

    public void setBarcodeResultListener(BarcodeResultListener barcodeResultListener) {
        this.barcodeResultListener = barcodeResultListener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionBarcode>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionBarcode> barcodes,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();

        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }

        for (int i = 0; i < barcodes.size(); ++i) {
            FirebaseVisionBarcode barcode = barcodes.get(i);
            BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
            graphicOverlay.add(barcodeGraphic);
        }

        graphicOverlay.postInvalidate();

        if(barcodeResultListener!=null)
            barcodeResultListener.onSuccess(originalCameraImage,barcodes,frameMetadata,graphicOverlay);

    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);

        if(barcodeResultListener!=null)
            barcodeResultListener.onFailure(e);
    }

    public interface BarcodeResultListener {
        void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionBarcode> barcodes,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

        void onFailure(@NonNull Exception e);
    }
}