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
package com.google.firebase.samples.apps.mlkit.java.custommodel;

import android.app.Activity;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.samples.apps.mlkit.common.BitmapUtils;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Custom Image Classifier Demo.
 */
public class CustomImageClassifierProcessor implements VisionImageProcessor {

    private static final String TAG = "Custom";
    private final CustomImageClassifier classifier;
    private final Reference<Activity> activityRef;

    public CustomImageClassifierProcessor(Activity activity, boolean useQuantizedModel) throws FirebaseMLException {
        activityRef = new WeakReference<>(activity);
        classifier = new CustomImageClassifier(activity.getApplicationContext(), useQuantizedModel);
    }

    @Override
    public void process(
            final ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay)
            throws FirebaseMLException {

        final Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        classifier
                .classifyFrame(data, frameMetadata.getWidth(), frameMetadata.getHeight())
                .addOnSuccessListener(
                        activity,
                        new OnSuccessListener<List<String>>() {
                            @Override
                            public void onSuccess(List<String> result) {
                                LabelGraphic labelGraphic = new LabelGraphic(graphicOverlay,
                                        result);
                                Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
                                CameraImageGraphic imageGraphic =
                                        new CameraImageGraphic(graphicOverlay, bitmap);
                                graphicOverlay.clear();
                                graphicOverlay.add(imageGraphic);
                                graphicOverlay.add(labelGraphic);
                                graphicOverlay.postInvalidate();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Custom classifier failed: " + e);
                                e.printStackTrace();
                            }
                        });
    }

    @Override
    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
        // nop
    }

    @Override
    public void stop() {
    }
}
