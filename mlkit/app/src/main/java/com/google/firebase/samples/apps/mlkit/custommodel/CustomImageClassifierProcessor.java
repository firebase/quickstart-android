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
package com.google.firebase.samples.apps.mlkit.custommodel;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.samples.apps.mlkit.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.VisionImageProcessor;

import java.nio.ByteBuffer;
import java.util.List;

/** Custom Image Classifier Demo. */
public class CustomImageClassifierProcessor implements VisionImageProcessor {

  private final CustomImageClassifier classifier;
  private final Activity activity;

  public CustomImageClassifierProcessor(Activity activity) throws FirebaseMLException {
    this.activity = activity;
    classifier = new CustomImageClassifier(activity);
  }

  @Override
  public void process(
          ByteBuffer data, FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay)
      throws FirebaseMLException {
    classifier
        .classifyFrame(data, frameMetadata.getWidth(), frameMetadata.getHeight())
        .addOnSuccessListener(
            activity,
            new OnSuccessListener<List<String>>() {
              @Override
              public void onSuccess(List<String> result) {
                LabelGraphic labelGraphic = new LabelGraphic(graphicOverlay);
                graphicOverlay.clear();
                graphicOverlay.add(labelGraphic);
                labelGraphic.updateLabel(result);
              }
            });
  }

  @Override
  public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
    // nop
  }

  @Override
  public void process(Image bitmap, int rotation, GraphicOverlay graphicOverlay) {
    // nop

  }

  @Override
  public void stop() {}
}
