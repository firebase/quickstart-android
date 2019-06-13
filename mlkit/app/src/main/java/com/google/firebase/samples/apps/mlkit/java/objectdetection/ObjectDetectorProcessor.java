package com.google.firebase.samples.apps.mlkit.java.objectdetection;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;

import java.io.IOException;
import java.util.List;

/** A processor to run object detector. */
public class ObjectDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionObject>> {

  private static final String TAG = "ObjectDetectorProcessor";

  private final FirebaseVisionObjectDetector detector;

  public ObjectDetectorProcessor(FirebaseVisionObjectDetectorOptions options) {
    detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
  }

  @Override
  public void stop() {
    super.stop();
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close object detector!", e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionObject>> detectInImage(FirebaseVisionImage image) {
    return detector.processImage(image);
  }

  @Override
  protected void onSuccess(
      @Nullable Bitmap originalCameraImage,
      @NonNull List<FirebaseVisionObject> results,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    if (originalCameraImage != null) {
      CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
      graphicOverlay.add(imageGraphic);
    }
    for (FirebaseVisionObject object : results) {
      ObjectGraphic objectGraphic = new ObjectGraphic(graphicOverlay, object);
      graphicOverlay.add(objectGraphic);
    }
    graphicOverlay.postInvalidate();
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Object detection failed!", e);
  }
}
