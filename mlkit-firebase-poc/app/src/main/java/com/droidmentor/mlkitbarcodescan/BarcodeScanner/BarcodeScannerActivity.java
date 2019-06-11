package com.droidmentor.mlkitbarcodescan.BarcodeScanner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.BarcodeScanningProcessor;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.BarcodeScanningProcessor.BarcodeResultListener;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.CameraSource;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.CameraSourcePreview;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.FrameMetadata;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay;
import com.droidmentor.mlkitbarcodescan.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droidmentor.mlkitbarcodescan.Util.BarcodeScanner.Constants.KEY_CAMERA_PERMISSION_GRANTED;
import static com.droidmentor.mlkitbarcodescan.Util.BarcodeScanner.Constants.PERMISSION_REQUEST_CAMERA;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class BarcodeScannerActivity extends AppCompatActivity {
    String TAG = "BarcodeScannerActivity";

    @BindView(R.id.barcodeOverlay) GraphicOverlay barcodeOverlay;
    @BindView(R.id.preview) CameraSourcePreview preview;
    @BindView(R.id.details_layout) LinearLayout mDetailsLayout;
    @BindView(R.id.scan_layout) RelativeLayout mScanLayout;
    @BindView(R.id.details_content) TextView mDetailsLabel;

    BarcodeScanningProcessor barcodeScanningProcessor;

    private CameraSource mCameraSource = null;


    boolean isCalled;
    boolean isAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        ButterKnife.bind(this);

        if (preview != null) {
            if (preview.isPermissionGranted(true, mMessageSender)) {
                new Thread(mMessageSender).start();
            }
        }
    }

    private void createCameraSource() {
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_PDF417)
                        .build();

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        // To connect the camera resource with the detector
        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

        barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
        barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());

        mCameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);
        startCameraSource();
    }

    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d(TAG, "startCameraSource: " + code);

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, PERMISSION_REQUEST_CAMERA);
            dlg.show();
        }

        if (mCameraSource != null && preview != null && barcodeOverlay != null) {
            try {
                Log.d(TAG, "startCameraSource: ");
                preview.start(mCameraSource, barcodeOverlay);
            }
            catch (IOException e) {
                Log.d(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
        else {
            Log.d(TAG, "startCameraSource: not started");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        preview.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (preview != null)
            preview.stop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isCalled = true;
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: ");

            if (preview != null)
                createCameraSource();
        }
    };

    private final Runnable mMessageSender = () -> {
        Log.d(TAG, "mMessageSender: ");
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CAMERA_PERMISSION_GRANTED, false);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    };

    public BarcodeResultListener getBarcodeResultListener() {

        return new BarcodeResultListener() {
            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionBarcode> barcodes, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                for (FirebaseVisionBarcode barCode : barcodes) {
                    FirebaseVisionBarcode.ContactInfo contactInfo = barCode.getContactInfo();

                    if (contactInfo != null) {
                        parseContactInfo(contactInfo);
                    }
                    else {
                        FirebaseVisionBarcode.DriverLicense driverLicense = barCode.getDriverLicense();

                        if (driverLicense != null) {
                            parseDriverLicenseInfo(driverLicense);
                        }
                        else if (barCode.getDisplayValue() != null) {
                            isAdded = true;
                            showDetails(barCode.getDisplayValue());
                        }
                    }
                }
            }

            private void showDetails(String label) {
                mDetailsLayout.setVisibility(View.VISIBLE);
                mScanLayout.setVisibility(View.GONE);

                // set label
                mDetailsLabel.setText(label);
            }

            private void parseDriverLicenseInfo(FirebaseVisionBarcode.DriverLicense driverLicense) {
                StringBuilder builder = new StringBuilder();
                isAdded = true;

                builder.append("Name : ");
                builder.append(driverLicense.getFirstName() + " " + driverLicense.getLastName() + "\n");

                builder.append("Age : ");
                builder.append(getAge(driverLicense.getBirthDate()) + "\n");

                builder.append("Gender : ");
                builder.append(getFormattedGender(driverLicense.getGender()) + "\n");

                builder.append("License ID : ");
                builder.append(driverLicense.getLicenseNumber() + "\n");

                builder.append("Expiry Date : ");
                builder.append(getDateInFormat(driverLicense.getExpiryDate(), null) + "\n");

                builder.append("Date Of Birth : ");
                builder.append(getDateInFormat(driverLicense.getBirthDate(), null) + "\n");

                builder.append("Country : ");
                builder.append(driverLicense.getIssuingCountry() + "\n");

                builder.append("Zip : ");
                builder.append(driverLicense.getAddressZip() + "\n");

                showDetails(builder.toString());
            }

            private String getAge(String dob) {
                return String.valueOf(getDiffYears(getFormattedDate(dob), new Date()));
            }

            public int getDiffYears(Date first, Date last) {
                Calendar a = getCalendar(first);
                Calendar b = getCalendar(last);

                int diff = b.get(YEAR) - a.get(YEAR);
                if (a.get(MONTH) > b.get(MONTH) ||
                        (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
                    diff--;
                }

                return diff;
            }

            private Calendar getCalendar(Date date) {
                Calendar cal = Calendar.getInstance(Locale.US);
                cal.setTime(date);
                return cal;
            }

            private String getFormattedGender(String gender) {
                switch (gender) {
                    case "1":
                        return "Male";

                    case "2":
                        return "Female";

                    default:
                        return "Other";
                }
            }

            private String getDateInFormat(String date, String pattern) {
                return new SimpleDateFormat(pattern != null ? pattern : "dd MMM, yyyy").format(getFormattedDate(date));
            }

            private Date getFormattedDate(String date) {
                SimpleDateFormat format = new SimpleDateFormat("mmddyyyy");

                try {
                    return format.parse(date);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }

                return null;
            }

            private void parseContactInfo(FirebaseVisionBarcode.ContactInfo contactInfo) {
                StringBuilder builder = new StringBuilder();
                isAdded = true;

                if (contactInfo.getName() != null && !TextUtils.isEmpty(contactInfo.getName().getFormattedName())) {
                    builder.append("Name : ");
                    builder.append(contactInfo.getName().getFormattedName());
                }

                if (contactInfo.getEmails().size() > 0) {
                    builder.append("Email Address : ");
                    builder.append(contactInfo.getEmails().get(0).getAddress());
                }

                if (contactInfo.getPhones().size() > 0) {
                    builder.append("Phone No. : ");
                    builder.append(contactInfo.getPhones().get(0).getNumber());
                }

                showDetails(builder.toString());
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
