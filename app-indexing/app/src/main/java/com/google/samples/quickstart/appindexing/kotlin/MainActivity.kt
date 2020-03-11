package com.google.samples.quickstart.appindexing.kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
// [START import_classes]
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
// [END import_classes]
import com.google.samples.quickstart.appindexing.R
import kotlinx.android.synthetic.main.activity_main.addStickersBtn
import kotlinx.android.synthetic.main.activity_main.clearStickersBtn
import kotlinx.android.synthetic.main.activity_main.link

class MainActivity : AppCompatActivity() {

    private var articleId: String? = null

    // [START handle_intent]
    override fun onCreate(savedInstanceState: Bundle?) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firebaseAppIndex = FirebaseAppIndex.getInstance()

        addStickersBtn.setOnClickListener { startService(Intent(baseContext, AppIndexingService::class.java)) }

        clearStickersBtn.setOnClickListener { AppIndexingUtil.clearStickers(baseContext, firebaseAppIndex) }
        // [END_EXCLUDE]
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        val data = intent.data
        if (Intent.ACTION_VIEW == action && data != null) {
            articleId = data.lastPathSegment
            link.text = data.toString()
        }
    }
    // [END handle_intent]

    // [START app_indexing_view]
    public override fun onStart() {
        super.onStart()

        if (articleId == null) {
            return
        }

        val baseUrl = Uri.parse("https://www.example.com/kotlin_articles/")
        val appUri = baseUrl.buildUpon().appendPath(articleId).build().toString()

        val articleToIndex = Indexable.Builder()
                .setName(TITLE)
                .setUrl(appUri)
                .build()

        val task = FirebaseAppIndex.getInstance().update(articleToIndex)

        // If the Task is already complete, a call to the listener will be immediately
        // scheduled
        task.addOnSuccessListener(this) { Log.d(TAG, "App Indexing API: Successfully added $TITLE to index") }

        task.addOnFailureListener(this) { exception ->
            Log.e(TAG, "App Indexing API: Failed to add $TITLE to index. ${exception.message}")
        }

        // log the view action
        val actionTask = FirebaseUserActions.getInstance().start(Actions.newView(TITLE,
                appUri))

        actionTask.addOnSuccessListener(this) {
            Log.d(TAG, "App Indexing API: Successfully started view action on $TITLE")
        }

        actionTask.addOnFailureListener(this) { exception ->
            Log.e(TAG, "App Indexing API: Failed to start view action on $TITLE. ${exception.message}")
        }
    }

    public override fun onStop() {
        super.onStop()
        if (articleId == null) {
            return
        }

        val baseUrl = Uri.parse("https://www.example.com/kotlin_articles/")
        val appUri = baseUrl.buildUpon().appendPath(articleId).build().toString()

        val actionTask = FirebaseUserActions.getInstance().end(Actions.newView(TITLE,
                appUri))

        actionTask.addOnSuccessListener(this) {
            Log.d(TAG, "App Indexing API: Successfully ended view action on $TITLE")
        }

        actionTask.addOnFailureListener(this) { exception ->
            Log.e(TAG, "App Indexing API: Failed to end view action on $TITLE. ${exception.message}")
        }
    }
    // [END app_indexing_view]

    companion object {

        private val TAG = MainActivity::class.java.name
        // Define a title for your current page, shown in autocompletion UI
        private const val TITLE = "Sample Article"
    }
}
