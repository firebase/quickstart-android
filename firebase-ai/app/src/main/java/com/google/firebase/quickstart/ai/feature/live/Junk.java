package com.google.firebase.quickstart.ai.feature.live;

import androidx.annotation.OptIn;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.LiveGenerativeModel;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.LiveGenerationConfig;
import com.google.firebase.ai.type.PublicPreviewAPI;
import com.google.firebase.ai.type.ResponseModality;

public class Junk {

    @OptIn(markerClass = PublicPreviewAPI.class)
    void a() {
        LiveGenerativeModel lm = FirebaseAI.getInstance(GenerativeBackend.googleAI()).liveModel(
                "MODEL_NAME",
                // Configure the model to respond with audio
                new LiveGenerationConfig.Builder()
                        .setResponseModality(ResponseModality.AUDIO)
                        .build()
        );

    }
}
