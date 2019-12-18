package com.google.firebase.samples.apps.mlkit.java.automl;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.samples.apps.mlkit.R;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;
import com.google.firebase.samples.apps.mlkit.java.labeldetector.LabelGraphic;
import com.google.firebase.samples.apps.mlkit.common.preference.PreferenceUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * AutoML image labeler Demo.
 */
public class AutoMLImageLabelerProcessor
    extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {

  private static final String TAG = "ODAutoMLILProcessor";

  private final Context context;
  private final FirebaseVisionImageLabeler detector;
  private final Task<Void> modelDownloadingTask;
  private final Mode mode;

  /**
   * The detection mode of the processor. Different modes will have different behavior on whether or
   * not waiting for the model download complete.
   */
  public enum Mode {
    STILL_IMAGE,
    LIVE_PREVIEW
  }

  public AutoMLImageLabelerProcessor(Context context, Mode mode) throws FirebaseMLException {
    this.context = context;
    this.mode = mode;

    String modelChoice = PreferenceUtils.getAutoMLRemoteModelChoice(context);
    if (modelChoice.equals(context.getString(R.string.pref_entries_automl_models_local))) {
      Log.d(TAG, "Local model used.");
      FirebaseAutoMLLocalModel localModel =
          new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("automl/manifest.json").build();
      detector =
          FirebaseVision.getInstance()
              .getOnDeviceAutoMLImageLabeler(
                  new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                      .setConfidenceThreshold(0)
                      .build());
      modelDownloadingTask = null;

    } else {
      Log.d(TAG, "Remote model used.");
      String remoteModelName = PreferenceUtils.getAutoMLRemoteModelName(context);
      FirebaseAutoMLRemoteModel remoteModel =
          new FirebaseAutoMLRemoteModel.Builder(remoteModelName).build();

      FirebaseModelDownloadConditions downloadConditions =
          new FirebaseModelDownloadConditions.Builder().requireWifi().build();
      modelDownloadingTask =
          FirebaseModelManager.getInstance().download(remoteModel, downloadConditions);
      detector =
          FirebaseVision.getInstance()
              .getOnDeviceAutoMLImageLabeler(
                  new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel)
                      .setConfidenceThreshold(0)
                      .build());
    }
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close the image labeler", e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionImageLabel>> detectInImage(final FirebaseVisionImage image) {
    if (modelDownloadingTask == null) {
      // No download task means only the locally bundled model is used. Model can be used directly.
      return detector.processImage(image);
    } else if (!modelDownloadingTask.isComplete()) {
      if (mode == Mode.LIVE_PREVIEW) {
        Log.i(TAG, "Model download is in progress. Skip detecting image.");
        return Tasks.forResult(Collections.<FirebaseVisionImageLabel>emptyList());
      } else {
        Log.i(TAG, "Model download is in progress. Waiting...");
        return modelDownloadingTask.continueWithTask(new Continuation<Void, Task<List<FirebaseVisionImageLabel>>>() {
          @Override
          public Task<List<FirebaseVisionImageLabel>> then(@NonNull Task<Void> task) {
            return processImageOnDownloadComplete(image);
          }
        });
      }
    } else {
      return processImageOnDownloadComplete(image);
    }
  }

  @Override
  protected void onSuccess(
      @Nullable Bitmap originalCameraImage,
      @NonNull List<FirebaseVisionImageLabel> labels,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    if (originalCameraImage != null) {
      CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
      graphicOverlay.add(imageGraphic);
    }
    LabelGraphic labelGraphic = new LabelGraphic(graphicOverlay, labels);
    graphicOverlay.add(labelGraphic);
    graphicOverlay.postInvalidate();
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Label detection failed.", e);
  }

  private Task<List<FirebaseVisionImageLabel>> processImageOnDownloadComplete(
      FirebaseVisionImage image) {
    if (modelDownloadingTask.isSuccessful()) {
      return detector.processImage(image);
    } else {
      String downloadingError = "Error downloading remote model.";
      Log.e(TAG, downloadingError, modelDownloadingTask.getException());
      Toast.makeText(context, downloadingError, Toast.LENGTH_SHORT).show();
      return Tasks.forException(
          new Exception("Failed to download remote model.", modelDownloadingTask.getException()));
    }
  }
}
