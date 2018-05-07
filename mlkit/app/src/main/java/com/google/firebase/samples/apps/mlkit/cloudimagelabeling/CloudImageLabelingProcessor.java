package com.google.firebase.samples.apps.mlkit.cloudimagelabeling;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.VisionProcessorBase;

import java.util.ArrayList;
import java.util.List;

/** Cloud Label Detector Demo. */
public class CloudImageLabelingProcessor
    extends VisionProcessorBase<List<FirebaseVisionCloudLabel>> {
  private static final String TAG = "CloudImageLabelingProcessor";

  private final FirebaseVisionCloudLabelDetector detector;

  public CloudImageLabelingProcessor() {
    FirebaseVisionCloudDetectorOptions options =
        new FirebaseVisionCloudDetectorOptions.Builder()
            .setMaxResults(10)
            .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
            .build();

    detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
  }

  @Override
  protected Task<List<FirebaseVisionCloudLabel>> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<FirebaseVisionCloudLabel> labels,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    Log.d(TAG, "cloud label size: " + labels.size());
    List<String> labelsStr = new ArrayList<>();
    for (int i = 0; i < labels.size(); ++i) {
      FirebaseVisionCloudLabel label = labels.get(i);
      Log.d(TAG, "cloud label: " + label);
      if (label.getLabel() != null) {
        labelsStr.add((label.getLabel()));
      }
    }
    CloudLabelGraphic cloudLabelGraphic = new CloudLabelGraphic(graphicOverlay);
    graphicOverlay.add(cloudLabelGraphic);
    cloudLabelGraphic.updateLabel(labelsStr);
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Cloud Label detection failed " + e);
  }
}
