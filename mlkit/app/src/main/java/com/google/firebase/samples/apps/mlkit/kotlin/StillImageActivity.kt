package com.google.firebase.samples.apps.mlkit.kotlin

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.*
import com.google.android.gms.common.annotation.KeepName
import com.google.firebase.samples.apps.mlkit.R
import com.google.firebase.samples.apps.mlkit.kotlin.cloudimagelabeling.CloudImageLabelingProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition.CloudDocumentTextRecognitionProcessor
import com.google.firebase.samples.apps.mlkit.kotlin.cloudtextrecognition.CloudTextRecognitionProcessor
import java.util.ArrayList
import java.io.IOException

import kotlinx.android.synthetic.main.activity_still_image.*

/** Activity demonstrating different image detector features with a still image from camera.  */
@KeepName
class StillImageActivity: AppCompatActivity() {

    private val TAG = "StillImageActivity"

    private val CLOUD_LABEL_DETECTION = "Cloud Label"
    private val CLOUD_LANDMARK_DETECTION = "Landmark"
    private val CLOUD_TEXT_DETECTION = "Cloud Text"
    private val CLOUD_DOCUMENT_TEXT_DETECTION = "Doc Text"

    private val SIZE_PREVIEW = "w:max" // Available on-screen width.
    private val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
    private val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio

    private val KEY_IMAGE_URI = "com.googletest.firebase.ml.demo.KEY_IMAGE_URI"
    private val KEY_IMAGE_MAX_WIDTH = "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_WIDTH"
    private val KEY_IMAGE_MAX_HEIGHT = "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_HEIGHT"
    private val KEY_SELECTED_SIZE = "com.googletest.firebase.ml.demo.KEY_SELECTED_SIZE"

    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CHOOSE_IMAGE = 1002

    private var preview: ImageView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var selectedMode = CLOUD_LABEL_DETECTION
    private var selectedSize: String? = SIZE_PREVIEW

    internal var isLandScape: Boolean = false

    private var imageUri: Uri? = null
    // Max width (portrait mode)
    private var imageMaxWidth: Int? = null
    // Max height (portrait mode)
    private var imageMaxHeight: Int? = null
    private var bitmapForDetection: Bitmap? = null
    private var imageProcessor: VisionImageProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_still_image)

        getImageButton.setOnClickListener(
                View.OnClickListener { view ->
                    // Menu for selecting either: a) take new photo b) select from existing
                    val popup = PopupMenu(this@StillImageActivity, view)
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
                })
        preview = findViewById<View>(R.id.previewPane) as ImageView
        if (preview == null) {
            Log.d(TAG, "Preview is null")
        }
        graphicOverlay = findViewById<View>(R.id.previewOverlay) as GraphicOverlay
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        populateFeatureSelector()
        populateSizeSelector()

        createImageProcessor()

        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
            imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE)

            if (imageUri != null) {
                tryReloadAndDetectInImage()
            }
        }
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
        featureSelector.adapter = dataAdapter
        featureSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                    parentView: AdapterView<*>, selectedItemView: View, pos: Int, id: Long) {
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
        sizeSelector.adapter = dataAdapter
        sizeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                    parentView: AdapterView<*>, selectedItemView: View, pos: Int, id: Long) {
                selectedSize = parentView.getItemAtPosition(pos).toString()
                tryReloadAndDetectInImage()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(KEY_IMAGE_URI, imageUri)
        if (imageMaxWidth != null) {
            outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth!!)
        }
        if (imageMaxHeight != null) {
            outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight!!)
        }
        outState.putString(KEY_SELECTED_SIZE, selectedSize)
    }

    private fun startCameraIntentForResult() {
        // Clean up last time's image
        imageUri = null
        preview?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.data
            tryReloadAndDetectInImage()
        }
    }

    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                return
            }

            // Clear the overlay first
            graphicOverlay!!.clear()

            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // Get the dimensions of the View
            val targetedSize = getTargetedWidthHeight()

            val targetWidth = targetedSize.first
            val maxHeight = targetedSize.second

            // Determine how much to scale down the image
            val scaleFactor = Math.max(
                    imageBitmap.width.toFloat() / targetWidth.toFloat(),
                    imageBitmap.height.toFloat() / maxHeight.toFloat())

            val resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap,
                    (imageBitmap.width / scaleFactor).toInt(),
                    (imageBitmap.height / scaleFactor).toInt(),
                    true)

            preview!!.setImageBitmap(resizedBitmap)
            bitmapForDetection = resizedBitmap

            imageProcessor!!.process(bitmapForDetection!!, graphicOverlay!!)
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
        }

    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxWidth(): Int? {
        if (imageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            if (isLandScape) {
                imageMaxWidth = (preview!!.parent as View).height - controlPanel.height
            } else {
                imageMaxWidth = (preview!!.parent as View).width
            }
        }

        return imageMaxWidth
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int? {
        if (imageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            if (isLandScape) {
                imageMaxHeight = (preview!!.parent as View).width
            } else {
                imageMaxHeight = (preview!!.parent as View).height - controlPanel.height
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
                val maxWidthForPortraitMode = getImageMaxWidth()!!
                val maxHeightForPortraitMode = getImageMaxHeight()!!
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
        when (selectedMode) {
            CLOUD_LABEL_DETECTION -> imageProcessor = CloudImageLabelingProcessor()
            CLOUD_LANDMARK_DETECTION -> imageProcessor = CloudLandmarkRecognitionProcessor()
            CLOUD_TEXT_DETECTION -> imageProcessor = CloudTextRecognitionProcessor()
            CLOUD_DOCUMENT_TEXT_DETECTION -> imageProcessor = CloudDocumentTextRecognitionProcessor()
            else -> throw IllegalStateException("Unknown selectedMode: $selectedMode")
        }
    }

}