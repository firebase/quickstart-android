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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A {@code FirebaseModelInterpreter} based image classifier.
 */
public class CustomImageClassifier {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "MLKitDemoApp:Classifier";

    /**
     * Name of the floating point model uploaded to the Firebase console.
     */
    private static final String REMOTE_FLOAT_MODEL_NAME = "mobilenet_float_v2_1.0_299";

    /**
     * Name of the quantized model uploaded to the Firebase console.
     */
    private static final String REMOTE_QUANT_MODEL_NAME = "mobilenet_quant_v2_1.0_299";

    /**
     * Name of the label file stored in Assets.
     */
    private static final String LABEL_PATH = "labels.txt";

    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;

    /**
     * Dimensions of inputs.
     */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    private static final int DIM_IMG_SIZE_X = 299;
    private static final int DIM_IMG_SIZE_Y = 299;
    private static final int QUANT_NUM_OF_BYTES_PER_CHANNEL = 1;
    private static final int FLOAT_NUM_OF_BYTES_PER_CHANNEL = 4;
    private Boolean mUseQuantizedModel;

    /* Preallocated buffers for storing image data in. */
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    /**
     * An instance of the driver class to run model inference with Firebase.
     */
    private FirebaseModelInterpreter interpreter;

    /**
     * Data configuration of input & output data of model.
     */
    private final FirebaseModelInputOutputOptions dataOptions;

    /**
     * Labels corresponding to the output of the vision model.
     */
    private final List<String> labelList;

    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1,
                                           Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    /**
     * Initializes an {@code CustomImageClassifier}.
     */
    CustomImageClassifier(final Context context, boolean useQuantizedModel) throws FirebaseMLException {
        mUseQuantizedModel = useQuantizedModel;
        final String remoteModelName = mUseQuantizedModel ? REMOTE_QUANT_MODEL_NAME :
                REMOTE_FLOAT_MODEL_NAME;
        final FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder(remoteModelName).build();
        final FirebaseModelManager firebaseModelManager = FirebaseModelManager.getInstance();
        firebaseModelManager
                .isModelDownloaded(remoteModel)
                .continueWithTask(
                        new Continuation<Boolean, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<Boolean> task) throws Exception {
                                // Create update condition if model is already downloaded,
                                // otherwise create download
                                // condition.
                                FirebaseModelDownloadConditions conditions =
                                        task.getResult()
                                                ? new FirebaseModelDownloadConditions.Builder()
                                                .requireWifi()
                                                .build() // Update condition that requires wifi.
                                                : new FirebaseModelDownloadConditions.Builder()
                                                .build(); // Download condition.
                                return firebaseModelManager.download(remoteModel, conditions);
                            }
                        })
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void ignored) {
                                FirebaseModelInterpreterOptions interpreterOptions =
                                        new FirebaseModelInterpreterOptions.Builder(
                                                new FirebaseCustomRemoteModel.Builder(remoteModelName).build())
                                                .build();
                                try {
                                    interpreter =
                                            FirebaseModelInterpreter.getInstance(interpreterOptions);
                                } catch (FirebaseMLException e) {
                                    Log.e(TAG, "Failed to build FirebaseModelInterpreter. ", e);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception ignored) {
                                Toast.makeText(
                                        context,
                                        "Model download failed for image classifier, please check" +
                                                " your connection.",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

        labelList = loadLabelList(context.getApplicationContext());
        Log.d(TAG, "Created a Custom Image Classifier.");
        int[] inputDims = {DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
        int[] outputDims = {1, labelList.size()};

        int dataType = mUseQuantizedModel ? FirebaseModelDataType.BYTE :
                FirebaseModelDataType.FLOAT32;
        dataOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, dataType, inputDims)
                        .setOutputFormat(0, dataType, outputDims)
                        .build();
        Log.d(TAG, "Configured input & output data for the custom image classifier.");
    }

    /**
     * Classifies a frame from the preview stream.
     */
    Task<List<String>> classifyFrame(ByteBuffer buffer, int width, int height)
            throws FirebaseMLException {
        if (interpreter == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            List<String> uninitialized = new ArrayList<>();
            uninitialized.add("Uninitialized Classifier.");
            Tasks.forResult(uninitialized);
        }
        // Create input data.
        ByteBuffer imgData = convertBitmapToByteBuffer(buffer, width, height);

        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(imgData).build();
        // Here's where the magic happens!!
        return interpreter
                .run(inputs, dataOptions)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get labels array: " + e.getMessage());
                        e.printStackTrace();
                    }
                })
                .continueWith(
                        new Continuation<FirebaseModelOutputs, List<String>>() {
                            @Override
                            public List<String> then(@NonNull Task<FirebaseModelOutputs> task) throws Exception {
                                if (mUseQuantizedModel) {
                                    byte[][] labelProbArray =
                                            task.getResult().<byte[][]>getOutput(0);
                                    return getTopLabels(labelProbArray);
                                } else {
                                    float[][] labelProbArray =
                                            task.getResult().<float[][]>getOutput(0);
                                    return getTopLabels(labelProbArray);

                                }
                            }
                        });
    }

    /**
     * Reads label list from Assets.
     */
    private List<String> loadLabelList(Context context) {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(context.getAssets().open(LABEL_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read label list.", e);
        }
        return labelList;
    }

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */
    private synchronized ByteBuffer convertBitmapToByteBuffer(
            ByteBuffer buffer, int width, int height) {
        int bytesPerChannel = mUseQuantizedModel ? QUANT_NUM_OF_BYTES_PER_CHANNEL :
                FLOAT_NUM_OF_BYTES_PER_CHANNEL;
        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        bytesPerChannel * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        Bitmap bitmap = createResizedBitmap(buffer, width, height);
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        // Convert the image to int points.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // Normalize the values according to the model used:
                // Quantized model expects a [0, 255] scale while a float model expects [0, 1].
                if (mUseQuantizedModel) {
                    imgData.put((byte) ((val >> 16) & 0xFF));
                    imgData.put((byte) ((val >> 8) & 0xFF));
                    imgData.put((byte) (val & 0xFF));
                } else {
                    imgData.putFloat(((val >> 16) & 0xFF) / 255.0f);
                    imgData.putFloat(((val >> 8) & 0xFF) / 255.0f);
                    imgData.putFloat((val & 0xFF) / 255.0f);
                }
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + (endTime - startTime));
        return imgData;
    }

    /**
     * Resizes image data from {@code ByteBuffer}.
     */
    private Bitmap createResizedBitmap(ByteBuffer buffer, int width, int height) {
        YuvImage img = new YuvImage(buffer.array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        img.compressToJpeg(new Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
    }

    private synchronized List<String> getTopLabels(byte[][] labelProbArray) {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i),
                            (labelProbArray[0][i] & 0xff) / 255.0f));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        return getTopKLabels();
    }

    private synchronized List<String> getTopLabels(float[][] labelProbArray) {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        return getTopKLabels();
    }

    /**
     * Gets the top-K labels, to be shown in UI as the results.
     */
    private synchronized List<String> getTopKLabels() {
        List<String> result = new ArrayList<>();
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            result.add(label.getKey() + ":" + label.getValue());
        }
        return result;
    }
}
