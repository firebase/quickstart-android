package com.google.firebase.samples.apps.mlkit.cloudtextrecognition;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudDocumentTextDetector;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.VisionProcessorBase;

/** Processor for the cloud document text detector demo. */
public class CloudDocumentTextRecognitionProcessor
    extends VisionProcessorBase<FirebaseVisionCloudText> {

  private static final String TAG = "CloudDocumentTextRecognitionProcessor";

  private final FirebaseVisionCloudDocumentTextDetector detector;

  public CloudDocumentTextRecognitionProcessor() {
    super();
    detector = FirebaseVision.getInstance().getVisionCloudDocumentTextDetector();
  }

  @Override
  protected Task<FirebaseVisionCloudText> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull FirebaseVisionCloudText text,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
      Log.d(TAG, "detected text is: " + text.getText());
      CloudTextGraphic textGraphic = new CloudTextGraphic(graphicOverlay, text);
      graphicOverlay.add(textGraphic);
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Cloud Document Text detection failed." + e);
  }
}
