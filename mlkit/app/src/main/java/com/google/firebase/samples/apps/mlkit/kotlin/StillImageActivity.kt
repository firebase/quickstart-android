package com.google.firebase.samples.apps.mlkit.kotlin

import android.app.Activity
import android.content.pm.PackageManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.google.firebase.samples.apps.mlkit.R
import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import com.google.firebase.samples.apps.mlkit.common.preference.SettingsActivity
import com.google.firebase.samples.apps.mlkit.common.preference.SettingsActivity.LaunchSource
import com.google.firebase.samples.apps.mlkit.databinding.ActivityStillImageBinding
import com.google.firebase.samples.apps.mlkit.kotlin.cloudimagelabeling.CloudImageLabelingProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition.CloudDocumentTextRecognitionProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition.CloudTextRecognitionProcessor
import java.io.IOException
import java.util.ArrayList
import kotlin.math.max

/** Activity demonstrating different image detector features with a still image from camera.  */
@KeepName
class StillImageActivity : AppCompatActivity() {

    private var selectedMode = CLOUD_LABEL_DETECTION
    private var selectedSize: String = SIZE_PREVIEW

    private var isLandScape: Boolean = false

    private var imageUri: Uri? = null
    // Max width (portrait mode)
    private var imageMaxWidth = 0
    // Max height (portrait mode)
    private var imageMaxHeight = 0
    private var imageProcessor: VisionImageProcessor? = null

    private lateinit var binding: ActivityStillImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStillImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getImageButton.setOnClickListener { view ->
                    // Menu for selecting either: a) take new photo b) select from existing
                    val popup = PopupMenu(this, view)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.select_images_from_local -> {
                                startChooseImageIntentForResult()
                                true
                            }
                            R.id.take_photo_using_camera -> {
                                startCameraIntentForResult()
                                true
                            }
                            else -> false
                        }
                    }

                    val inflater = popup.menuInflater
                    inflater.inflate(R.menu.camera_button_menu, popup.menu)
                    popup.show()
                }

        populateFeatureSelector()
        populateSizeSelector()

        createImageProcessor()

        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri = it.getParcelable(KEY_IMAGE_URI)
            imageMaxWidth = it.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight = it.getInt(KEY_IMAGE_MAX_HEIGHT)
            selectedSize = it.getString(KEY_SELECTED_SIZE, "")

            imageUri?.let { _ ->
                tryReloadAndDetectInImage()
            }
        }

        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createImageProcessor()
        tryReloadAndDetectInImage()
    }

    private fun getRequiredPermissions(): Array<String?> {
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

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.still_image_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.STILL_IMAGE)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun populateFeatureSelector() {
        val options = ArrayList<String>()
        options.add(CLOUD_LABEL_DETECTION)
        options.add(CLOUD_LANDMARK_DETECTION)
        options.add(CLOUD_TEXT_DETECTION)
        options.add(CLOUD_DOCUMENT_TEXT_DETECTION)
        // Creating adapter for featureSpinner
        val dataAdapter = ArrayAdapter(this, R.layout.spinner_style, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner
        binding.featureSelector.adapter = dataAdapter
        binding.featureSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                pos: Int,
                id: Long
            ) {
                selectedMode = parentView.getItemAtPosition(pos).toString()
                createImageProcessor()
                tryReloadAndDetectInImage()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    private fun populateSizeSelector() {
        val options = ArrayList<String>()
        options.add(SIZE_PREVIEW)
        options.add(SIZE_1024_768)
        options.add(SIZE_640_480)

        // Creating adapter for featureSpinner
        val dataAdapter = ArrayAdapter(this, R.layout.spinner_style, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner
        binding.sizeSelector.adapter = dataAdapter
        binding.sizeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                pos: Int,
                id: Long
            ) {
                selectedSize = parentView.getItemAtPosition(pos).toString()
                tryReloadAndDetectInImage()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        with(outState) {
            putParcelable(KEY_IMAGE_URI, imageUri)
            putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth)
            putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight)
            putString(KEY_SELECTED_SIZE, selectedSize)
        }
    }

    private fun startCameraIntentForResult() {
        // Clean up last time's image
        imageUri = null
        binding.previewPane.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage()
        }
    }

    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                return
            }

            // Clear the overlay first
            binding.previewOverlay.clear()

            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }

            // Get the dimensions of the View
            val targetedSize = getTargetedWidthHeight()

            val targetWidth = targetedSize.first
            val maxHeight = targetedSize.second

            // Determine how much to scale down the image
            val scaleFactor = max(
                    imageBitmap.width.toFloat() / targetWidth.toFloat(),
                    imageBitmap.height.toFloat() / maxHeight.toFloat())

            val resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap,
                    (imageBitmap.width / scaleFactor).toInt(),
                    (imageBitmap.height / scaleFactor).toInt(),
                    true)

            binding.previewPane.setImageBitmap(resizedBitmap)
            resizedBitmap?.let {
                imageProcessor?.process(it, binding.previewOverlay)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
        }
    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxWidth(): Int {
        if (imageMaxWidth == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxWidth = if (isLandScape) {
                (binding.previewPane.parent as View).height - binding.controlPanel.height
            } else {
                (binding.previewPane.parent as View).width
            }
        }

        return imageMaxWidth
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int {
        if (imageMaxHeight == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxHeight = if (isLandScape) {
                (binding.previewPane.parent as View).width
            } else {
                (binding.previewPane.parent as View).height - binding.controlPanel.height
            }
        }

        return imageMaxHeight
    }

    // Gets the targeted width / height.
    private fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int

        when (selectedSize) {
            SIZE_PREVIEW -> {
                val maxWidthForPortraitMode = getImageMaxWidth()
                val maxHeightForPortraitMode = getImageMaxHeight()
                targetWidth = if (isLandScape) maxHeightForPortraitMode else maxWidthForPortraitMode
                targetHeight = if (isLandScape) maxWidthForPortraitMode else maxHeightForPortraitMode
            }
            SIZE_640_480 -> {
                targetWidth = if (isLandScape) 640 else 480
                targetHeight = if (isLandScape) 480 else 640
            }
            SIZE_1024_768 -> {
                targetWidth = if (isLandScape) 1024 else 768
                targetHeight = if (isLandScape) 768 else 1024
            }
            else -> throw IllegalStateException("Unknown size")
        }

        return Pair(targetWidth, targetHeight)
    }

    private fun createImageProcessor() {
        imageProcessor = when (selectedMode) {
            CLOUD_LABEL_DETECTION -> CloudImageLabelingProcessor()
            CLOUD_LANDMARK_DETECTION -> CloudLandmarkRecognitionProcessor()
            CLOUD_TEXT_DETECTION -> CloudTextRecognitionProcessor()
            CLOUD_DOCUMENT_TEXT_DETECTION -> CloudDocumentTextRecognitionProcessor()
            else -> throw IllegalStateException("Unknown selectedMode: $selectedMode")
        }
    }

    companion object {

        private const val TAG = "StillImageActivity"

        private const val PERMISSION_REQUESTS = 1

        private const val CLOUD_LABEL_DETECTION = "Cloud Label"
        private const val CLOUD_LANDMARK_DETECTION = "Landmark"
        private const val CLOUD_TEXT_DETECTION = "Cloud Text"
        private const val CLOUD_DOCUMENT_TEXT_DETECTION = "Doc Text"

        private const val SIZE_PREVIEW = "w:max" // Available on-screen width.
        private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
        private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio

        private const val KEY_IMAGE_URI = "com.googletest.firebase.ml.demo.KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH = "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT = "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_HEIGHT"
        private const val KEY_SELECTED_SIZE = "com.googletest.firebase.ml.demo.KEY_SELECTED_SIZE"

        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_CHOOSE_IMAGE = 1002
    }
}
