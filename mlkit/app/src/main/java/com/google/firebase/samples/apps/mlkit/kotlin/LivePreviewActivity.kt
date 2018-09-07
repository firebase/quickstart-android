package com.google.firebase.samples.apps.mlkit.kotlin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.common.annotation.KeepName
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.samples.apps.mlkit.kotlin.barcodescanning.BarcodeScanningProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.custommodel.CustomImageClassifierProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.facedetection.FaceDetectionProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.imagelabeling.ImageLabelingProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.textrecognition.CameraSourcePreview
import com.google.firebase.samples.apps.mlkit.kotlin.textrecognition.TextRecognitionProcessor
import java.io.IOException
import java.util.*
import kotlinx.android.synthetic.main.activity_live_preview.*

/** Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.  */
@KeepName
class LivePreviewActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var selectedModel = FACE_DETECTION

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_live_preview)

        preview = findViewById<View>(R.id.firePreview) as CameraSourcePreview
        if (preview == null) {
            Log.d(TAG, "Preview is null")
        }
        graphicOverlay = findViewById<View>(R.id.fireFaceOverlay) as GraphicOverlay
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        val spinner = findViewById<View>(R.id.spinner) as Spinner
        val options = ArrayList<String>()
        options.add(FACE_DETECTION)
        options.add(TEXT_DETECTION)
        options.add(BARCODE_DETECTION)
        options.add(IMAGE_LABEL_DETECTION)
        options.add(CLASSIFICATION)
        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, R.layout.spinner_style, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner
        spinner.adapter = dataAdapter
        spinner.onItemSelectedListener = this

        val facingSwitch = findViewById<View>(R.id.facingswitch) as ToggleButton
        facingSwitch.setOnCheckedChangeListener(this)

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        } else {
            getRuntimePermissions()
        }
    }

    @Synchronized
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString()
        Log.d(TAG, "Selected model: $selectedModel")
        preview!!.stop()
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
            startCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Do nothing.
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_FRONT)
            } else {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
            }
        }
        preview!!.stop()
        startCameraSource()
    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay!!)
        }

        try {
            when (model) {
                CLASSIFICATION -> {
                    Log.i(TAG, "Using Custom Image Classifier Processor")
                    cameraSource!!.setMachineLearningFrameProcessor(CustomImageClassifierProcessor(this))
                }
                TEXT_DETECTION -> {
                    Log.i(TAG, "Using Text Detector Processor")
                    cameraSource!!.setMachineLearningFrameProcessor(TextRecognitionProcessor())
                }
                FACE_DETECTION -> {
                    Log.i(TAG, "Using Face Detector Processor")
                    cameraSource!!.setMachineLearningFrameProcessor(FaceDetectionProcessor())
                }
                BARCODE_DETECTION -> {
                    Log.i(TAG, "Using Barcode Detector Processor")
                    cameraSource!!.setMachineLearningFrameProcessor(BarcodeScanningProcessor())
                }
                IMAGE_LABEL_DETECTION -> {
                    Log.i(TAG, "Using Image Label Detector Processor")
                    cameraSource!!.setMachineLearningFrameProcessor(ImageLabelingProcessor())
                }
                else -> Log.e(TAG, "Unknown model: $model")
            }
        } catch (e: FirebaseMLException) {
            Log.e(TAG, "can not create camera source: $model")
        }

    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource!!, graphicOverlay!!)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }

        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val FACE_DETECTION = "Face Detection"
        private const val TEXT_DETECTION = "Text Detection"
        private const val BARCODE_DETECTION = "Barcode Detection"
        private const val IMAGE_LABEL_DETECTION = "Label Detection"
        private const val CLASSIFICATION = "Classification"
        private const val TAG = "LivePreviewActivity"
        private const val PERMISSION_REQUESTS = 1

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}