package com.google.firebase.quickstart.fcm.kotlin

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

class UpdateTokenWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Refresh the token and send it to your server
        var token = Firebase.messaging.token.await()

        // If you have your own server, call API to send the above token and Date() for this user's device

        // Example shown below with Firestore
        // Add token and timestamp to Firestore for this user
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp(),
        )

        // Get user ID from Firebase Auth or your own server
        Firebase.firestore.collection("fcmTokens").document("myuserid")
            .set(deviceToken).await()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}