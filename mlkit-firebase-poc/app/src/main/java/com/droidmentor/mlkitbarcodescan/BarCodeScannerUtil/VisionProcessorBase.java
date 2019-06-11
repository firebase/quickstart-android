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
package com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil;

import android.graphics.Bitmap;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.BitmapUtils;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.FrameMetadata;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.VisionImageProcessor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(Bitmap, Object, FrameMetadata, GraphicOverlay)} to define what they want to with
 * the detection results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector
 * object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")

    private FrameMetadata processingMetaData;

    public VisionProcessorBase() {
    }

    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay graphicOverlay) throws FirebaseMLException {
        latestImage = data;
        latestImageMetaData = frameMetadata;

        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay);
        }
    }

    @Override
    public void process(Bitmap bitmap, com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay graphicOverlay) {
        detectInVisionImage(null /* bitmap */, FirebaseVisionImage.fromBitmap(bitmap), null,
                graphicOverlay);
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;

        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay) {

        // To create the FirebaseVisionImage

        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteBuffer(data, metadata);

        detectInVisionImage(
                bitmap, firebaseVisionImage, frameMetadata,
                graphicOverlay);
    }

    private void detectInVisionImage(
            final Bitmap originalCameraImage, FirebaseVisionImage image,
            final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(
                        results -> {
                            VisionProcessorBase.this.onSuccess(originalCameraImage, results,
                                    metadata,
                                    graphicOverlay);
                            processLatestImage(graphicOverlay);
                        })
                .addOnFailureListener(
                        VisionProcessorBase.this::onFailure);
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     *                            image.
     */
    protected abstract void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);
}
