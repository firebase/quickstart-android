// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.samples.apps.mlkit.java.cloudlandmarkrecognition;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;

import java.util.List;

/**
 * Cloud Landmark Detector Demo.
 */
public class CloudLandmarkRecognitionProcessor
        extends VisionProcessorBase<List<FirebaseVisionCloudLandmark>> {
    private static final String TAG = "CloudLmkRecProc";

    private final FirebaseVisionCloudLandmarkDetector detector;

    public CloudLandmarkRecognitionProcessor() {
        super();
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setMaxResults(10)
                        .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
                        .build();

        detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options);
    }

    @Override
    protected Task<List<FirebaseVisionCloudLandmark>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionCloudLandmark> landmarks,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        Log.d(TAG, "cloud landmark size: " + landmarks.size());
        for (int i = 0; i < landmarks.size(); ++i) {
            FirebaseVisionCloudLandmark landmark = landmarks.get(i);
            Log.d(TAG, "cloud landmark: " + landmark);
            CloudLandmarkGraphic cloudLandmarkGraphic = new CloudLandmarkGraphic(graphicOverlay,
                    landmark);
            graphicOverlay.add(cloudLandmarkGraphic);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Cloud Landmark detection failed " + e);
    }
}
