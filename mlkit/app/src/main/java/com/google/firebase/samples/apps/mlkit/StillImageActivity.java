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
package com.google.firebase.samples.apps.mlkit;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;

import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.samples.apps.mlkit.cloudimagelabeling.CloudImageLabelingProcessor;
import com.google.firebase.samples.apps.mlkit.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor;
import com.google.firebase.samples.apps.mlkit.cloudtextrecognition.CloudDocumentTextRecognitionProcessor;


import com.google.firebase.samples.apps.mlkit.cloudtextrecognition.CloudTextRecognitionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Activity demonstrating different image detector features with a still image from camera. */
@KeepName
public final class StillImageActivity extends AppCompatActivity {

  private static final String TAG = "StillImageActivity";

  private static final String CLOUD_LABEL_DETECTION = "Cloud Label";
  private static final String CLOUD_LANDMARK_DETECTION = "Landmark";
  private static final String CLOUD_TEXT_DETECTION = "Cloud Text";
  private static final String CLOUD_DOCUMENT_TEXT_DETECTION = "Doc Text";

  private static final String SIZE_PREVIEW = "w:max"; // Available on-screen width.
  private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
  private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio

  private static final String KEY_IMAGE_URI = "com.googletest.firebase.ml.demo.KEY_IMAGE_URI";
  private static final String KEY_IMAGE_MAX_WIDTH =
      "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_WIDTH";
  private static final String KEY_IMAGE_MAX_HEIGHT =
      "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_HEIGHT";
  private static final String KEY_SELECTED_SIZE =
      "com.googletest.firebase.ml.demo.KEY_SELECTED_SIZE";

  private static final int REQUEST_IMAGE_CAPTURE = 1001;
  private static final int REQUEST_CHOOSE_IMAGE = 1002;

  private Button getImageButton;
  private ImageView preview;
  private GraphicOverlay graphicOverlay;
  private String selectedMode = CLOUD_LABEL_DETECTION;
  private String selectedSize = SIZE_PREVIEW;

  boolean isLandScape;

  private Uri imageUri;
  // Max width (portrait mode)
  private Integer imageMaxWidth;
  // Max height (portrait mode)
  private Integer imageMaxHeight;
  private Bitmap bitmapForDetection;
  private VisionImageProcessor imageProcessor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_still_image);

    getImageButton = (Button) findViewById(R.id.getImageButton);
    getImageButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            // Menu for selecting either: a) take new photo b) select from existing
            PopupMenu popup = new PopupMenu(StillImageActivity.this, view);
            popup.setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                      case R.id.select_images_from_local:
                        startChooseImageIntentForResult();
                        return true;
                      case R.id.take_photo_using_camera:
                        startCameraIntentForResult();
                        return true;
                      default:
                        return false;
                    }
                  }
                });

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
            popup.show();
          }
        });
    preview = (ImageView) findViewById(R.id.previewPane);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = (GraphicOverlay) findViewById(R.id.previewOverlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    populateFeatureSelector();
    populateSizeSelector();

    createImageProcessor();

    isLandScape =
        (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

    if (savedInstanceState != null) {
      imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
      imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH);
      imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT);
      selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);

      if (imageUri != null) {
        tryReloadAndDetectInImage();
      }
    }
  }

  private void populateFeatureSelector() {
    Spinner featureSpinner = (Spinner) findViewById(R.id.featureSelector);
    List<String> options = new ArrayList<>();
    options.add(CLOUD_LABEL_DETECTION);
    options.add(CLOUD_LANDMARK_DETECTION);
    options.add(CLOUD_TEXT_DETECTION);
    options.add(CLOUD_DOCUMENT_TEXT_DETECTION);
    // Creating adapter for featureSpinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    featureSpinner.setAdapter(dataAdapter);
    featureSpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {

          @Override
          public void onItemSelected(
                  AdapterView<?> parentView, View selectedItemView, int pos, long id) {
            selectedMode = parentView.getItemAtPosition(pos).toString();
            createImageProcessor();
            tryReloadAndDetectInImage();
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {}
        });
  }

  private void populateSizeSelector() {
    Spinner sizeSpinner = (Spinner) findViewById(R.id.sizeSelector);
    List<String> options = new ArrayList<>();
    options.add(SIZE_PREVIEW);
    options.add(SIZE_1024_768);
    options.add(SIZE_640_480);

    // Creating adapter for featureSpinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    sizeSpinner.setAdapter(dataAdapter);
    sizeSpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {

          @Override
          public void onItemSelected(
                  AdapterView<?> parentView, View selectedItemView, int pos, long id) {
            selectedSize = parentView.getItemAtPosition(pos).toString();
            tryReloadAndDetectInImage();
          }

          @Override
          public void onNothingSelected(AdapterView<?> arg0) {}
        });
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putParcelable(KEY_IMAGE_URI, imageUri);
    if (imageMaxWidth != null) {
      outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth);
    }
    if (imageMaxHeight != null) {
      outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight);
    }
    outState.putString(KEY_SELECTED_SIZE, selectedSize);
  }

  private void startCameraIntentForResult() {
    // Clean up last time's image
    imageUri = null;
    preview.setImageBitmap(null);

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.TITLE, "New Picture");
      values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
      imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  private void startChooseImageIntentForResult() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      tryReloadAndDetectInImage();
    } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
      // In this case, imageUri is returned by the chooser, save it.
      imageUri = data.getData();
      tryReloadAndDetectInImage();
    }
  }

  private void tryReloadAndDetectInImage() {
    try {
      if (imageUri == null) {
        return;
      }

      // Clear the overlay first
      graphicOverlay.clear();

      Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

      // Get the dimensions of the View
      Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

      int targetWidth = targetedSize.first;
      int maxHeight = targetedSize.second;

      // Determine how much to scale down the image
      float scaleFactor =
          Math.max(
              (float) imageBitmap.getWidth() / (float) targetWidth,
              (float) imageBitmap.getHeight() / (float) maxHeight);

      Bitmap resizedBitmap =
          Bitmap.createScaledBitmap(
              imageBitmap,
              (int) (imageBitmap.getWidth() / scaleFactor),
              (int) (imageBitmap.getHeight() / scaleFactor),
              true);

      preview.setImageBitmap(resizedBitmap);
      bitmapForDetection = resizedBitmap;

      imageProcessor.process(bitmapForDetection, graphicOverlay);
    } catch (IOException e) {
      Log.e(TAG, "Error retrieving saved image");
    }
  }

  // Returns max image width, always for portrait mode. Caller needs to swap width / height for
  // landscape mode.
  private Integer getImageMaxWidth() {
    if (imageMaxWidth == null) {
      // Calculate the max width in portrait mode. This is done lazily since we need to wait for
      // a UI layout pass to get the right values. So delay it to first time image rendering time.
      if (isLandScape) {
        imageMaxWidth =
            ((View) preview.getParent()).getHeight() - findViewById(R.id.controlPanel).getHeight();
      } else {
        imageMaxWidth = ((View) preview.getParent()).getWidth();
      }
    }

    return imageMaxWidth;
  }

  // Returns max image height, always for portrait mode. Caller needs to swap width / height for
  // landscape mode.
  private Integer getImageMaxHeight() {
    if (imageMaxHeight == null) {
      // Calculate the max width in portrait mode. This is done lazily since we need to wait for
      // a UI layout pass to get the right values. So delay it to first time image rendering time.
      if (isLandScape) {
        imageMaxHeight = ((View) preview.getParent()).getWidth();
      } else {
        imageMaxHeight =
            ((View) preview.getParent()).getHeight() - findViewById(R.id.controlPanel).getHeight();
      }
    }

    return imageMaxHeight;
  }

  // Gets the targeted width / height.
  private Pair<Integer, Integer> getTargetedWidthHeight() {
    int targetWidth;
    int targetHeight;

    switch (selectedSize) {
      case SIZE_PREVIEW:
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = isLandScape ? maxHeightForPortraitMode : maxWidthForPortraitMode;
        targetHeight = isLandScape ? maxWidthForPortraitMode : maxHeightForPortraitMode;
        break;
      case SIZE_640_480:
        targetWidth = isLandScape ? 640 : 480;
        targetHeight = isLandScape ? 480 : 640;
        break;
      case SIZE_1024_768:
        targetWidth = isLandScape ? 1024 : 768;
        targetHeight = isLandScape ? 768 : 1024;
        break;
      default:
        throw new IllegalStateException("Unknown size");
    }

    return new Pair<>(targetWidth, targetHeight);
  }

  private void createImageProcessor() {
    switch (selectedMode) {
      case CLOUD_LABEL_DETECTION:
        imageProcessor = new CloudImageLabelingProcessor();
        break;
      case CLOUD_LANDMARK_DETECTION:
        imageProcessor = new CloudLandmarkRecognitionProcessor();
        break;
      case CLOUD_TEXT_DETECTION:
        imageProcessor = new CloudTextRecognitionProcessor();
        break;
      case CLOUD_DOCUMENT_TEXT_DETECTION:
        imageProcessor = new CloudDocumentTextRecognitionProcessor();
        break;
      default:
        throw new IllegalStateException("Unknown selectedMode: " + selectedMode);
    }
  }
}
