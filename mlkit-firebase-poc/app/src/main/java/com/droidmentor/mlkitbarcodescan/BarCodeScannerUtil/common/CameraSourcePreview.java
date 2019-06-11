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
package com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;

import java.io.IOException;

import static com.droidmentor.mlkitbarcodescan.Util.BarcodeScanner.Constants.PERMISSION_REQUEST_CAMERA;

/** Preview the camera image in the screen. */
public class CameraSourcePreview extends ViewGroup {
  private static final String TAG = "MIDemoApp:Preview";

  private Context context;
  private SurfaceView surfaceView;
  private boolean startRequested;
  private boolean surfaceAvailable;
  private CameraSource cameraSource;

  private GraphicOverlay overlay;

  private String CAMERA_PERMISSION = Manifest.permission.CAMERA;

  private boolean showRequestPopup;
  private Fragment currentFragment;
  public boolean isActionPending=true;
  private boolean isPermissionGranted;


  Runnable mCameraPermissionSender;

  public CameraSourcePreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    startRequested = false;
    surfaceAvailable = false;

    surfaceView = new SurfaceView(context);
    surfaceView.getHolder().addCallback(new SurfaceCallback());
    addView(surfaceView);
  }

  public void start(CameraSource cameraSource) throws IOException {
    if (cameraSource == null) {
      stop();
    }

    this.cameraSource = cameraSource;

    if (this.cameraSource != null) {
      startRequested = true;
      startIfReady();
    }
  }

  public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
    this.overlay = overlay;
    start(cameraSource);
  }

  public void stop() {
    if (cameraSource != null) {
      cameraSource.stop();
    }
  }

  public void release() {
    if (cameraSource != null) {
      cameraSource.release();
      cameraSource = null;
    }
  }

  @SuppressLint("MissingPermission")
  private void startIfReady() throws IOException {
    if (startRequested && surfaceAvailable) {
      cameraSource.start();
      if (overlay != null) {
        Size size = cameraSource.getPreviewSize();
        int min = Math.min(size.getWidth(), 2 * size.getHeight());
        int max = Math.max(size.getWidth(), 2 * size.getHeight());
        if (isPortraitMode()) {
          // Swap width and height sizes when in portrait, since it will be rotated by
          // 90 degrees
          overlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
        } else {
          overlay.setCameraInfo(max, min, cameraSource.getCameraFacing());
        }
        overlay.clear();
      }
      startRequested = false;
    }
  }

  private class SurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder surface) {
      surfaceAvailable = true;
      try {
        startIfReady();
      } catch (IOException e) {
        Log.e(TAG, "Could not start camera source.", e);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surface) {
      surfaceAvailable = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int previewWidth = 320;
    int previewHeight = 240;
    if (cameraSource != null) {
      Size size = cameraSource.getPreviewSize();
      if (size != null) {
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
      }
    }

    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
    if (isPortraitMode()) {
      int tmp = previewWidth;
      previewWidth = previewHeight;
      previewHeight = tmp;
    }

    final int viewWidth = right - left;
    final int viewHeight = bottom - top;

    int childWidth;
    int childHeight;
    int childXOffset = 0;
    int childYOffset = 0;
    float widthRatio = (float) viewWidth / (float) previewWidth;
    float heightRatio = (float) viewHeight / (float) previewHeight;

    // To fill the view with the camera preview, while also preserving the correct aspect ratio,
    // it is usually necessary to slightly oversize the child and to crop off portions along one
    // of the dimensions.  We scale up based on the dimension requiring the most correction, and
    // compute a crop offset for the other dimension.
    if (widthRatio > heightRatio) {
      childWidth = viewWidth;
      childHeight = (int) ((float) previewHeight * widthRatio);
      childYOffset = (childHeight - viewHeight) / 2;
    } else {
      childWidth = (int) ((float) previewWidth * heightRatio);
      childHeight = viewHeight;
      childXOffset = (childWidth - viewWidth) / 2;
    }

    for (int i = 0; i < getChildCount(); ++i) {
      // One dimension will be cropped.  We shift child over or up by this offset and adjust
      // the size to maintain the proper aspect ratio.
      getChildAt(i).layout(
              -1 * childXOffset, -1 * childYOffset,
              childWidth - childXOffset, childHeight - childYOffset);
    }

    try {
      startIfReady();
    } catch (IOException e) {
      Log.e(TAG, "Could not start camera source.", e);
    }
  }

  private boolean isPortraitMode() {
    int orientation = context.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return false;
    }
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      return true;
    }

    Log.d(TAG, "isPortraitMode returning false by default");
    return false;
  }

  /**
   * To check whether the needed permission is granted
   *
   * @return
   */
  public boolean isPermissionGranted() {
    return isPermissionGranted(showRequestPopup);
  }

  /**
   * To check whether the needed permission is granted
   *
   * @param showRequestPopup - Flag to show the permission check popup
   * @return
   */
  public boolean isPermissionGranted(boolean showRequestPopup) {
    return isPermissionGranted(showRequestPopup,null);
  }

  /**
   * To check whether the needed permission is granted
   *
   * @param showRequestPopup - Flag to show the permission check popup
   * @param mCameraPermissionSender - Runnable to handle the callbacks based on the permission status
   * @return
   */
  public boolean isPermissionGranted(boolean showRequestPopup, Runnable mCameraPermissionSender) {

    this.mCameraPermissionSender=mCameraPermissionSender;

    if (Build.VERSION.SDK_INT >= 23) {
      if (ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
              == PackageManager.PERMISSION_GRANTED) {
        Log.v(TAG, "Permission is granted");
        return true;
      } else {

        Log.v(TAG, "Permission is revoked");
        if (showRequestPopup) {
          if (currentFragment != null) {
            currentFragment.requestPermissions(new String[]{CAMERA_PERMISSION}, PERMISSION_REQUEST_CAMERA);
          } else
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{CAMERA_PERMISSION}, PERMISSION_REQUEST_CAMERA);
        }
        return false;
      }
    } else {
      //permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission is granted");
      return true;
    }
  }


  /**
   * Permission request result handling
   *
   * @param requestCode   - Permission request identification code
   * @param permissions   - Requested permissions
   * @param grantResults- Status of the requested permissions
   */

  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0)
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        isPermissionGranted = true;
        if (isActionPending) {
          isActionPending = false;

          if(mCameraPermissionSender!=null)
            new Thread(mCameraPermissionSender).start();

          // makeCall(pendingPhoneNumber);
        }
      } else {
        if (currentFragment != null) {
          if (!currentFragment.shouldShowRequestPermissionRationale(CAMERA_PERMISSION)) {
            showRequestPermissionRationale();
          } else {
            Log.d(TAG, "onRequestPermissionsResult: Permission is not provided");

          }
        } else {
          if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, CAMERA_PERMISSION)) {
            showRequestPermissionRationale();
          } else {
            Log.d(TAG, "onRequestPermissionsResult: Permission is not provided");

          }
        }
      }
  }

  public void showRequestPermissionRationale() {
    Log.i("Go to settings", "and enable permissions");

    // To redirect the settings-> app details page

    Intent intent = new Intent();
    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
    intent.setData(uri);
    context.startActivity(intent);
  }

  // Setter functions

  // To set whether the util need to show the permission request popup or not

  public void setShowRequestPopup(boolean showRequestPopup) {
    this.showRequestPopup = showRequestPopup;
  }

  // To show the source is Activity or Fragment

  public void setCurrentFragment(Fragment currentFragment) {
    this.currentFragment = currentFragment;
  }


}
