/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.quickstart.usermanagement;

import android.content.Context;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

/**
 * Example class for minting JSON Web Tokens for use with custom authentication.
 * In a real application this should always be done on a secure webserver for performance and
 * security reasons. You should never bundle the secrets JSON file in a publicly available binary
 * such as an Android or iOS application.
 *
 * <b>NOTE: this is for demonstration purposes only. Never do this in a real application!</b>
 */
public class MockTokenServer {

    private static final String TAG = "MockTokenServer";

    private JsonFactory mFactory;
    private HttpTransport mTransport;

    private Context mContext;

    public MockTokenServer(Context context) {
        this.mContext = context;
        mFactory = JacksonFactory.getDefaultInstance();
        mTransport = new NetHttpTransport();
    }

    public String getCustomToken(String userId) {
        // Read JSON file with Service Account credentials
        GoogleCredential credential = readJsonFile();
        if (credential == null) {
            Log.w(TAG, "Null credential.");
            return null;
        }

        // Private key from credentials
        PrivateKey privateKey = credential.getServiceAccountPrivateKey();

        // JWT Header
        JsonWebSignature.Header header = new JsonWebSignature.Header()
                .setAlgorithm("RS256")
                .setType("JWT")
                .setKeyId(credential.getServiceAccountPrivateKeyId());

        // JWT Payload
        long nowSeconds = System.currentTimeMillis() / 1000;
        JsonWebSignature.Payload payload = new JsonWebSignature.Payload()
                .setIssuer(credential.getServiceAccountId())
                .setAudience("https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit")
                .setIssuedAtTimeSeconds(nowSeconds)
                .setExpirationTimeSeconds(nowSeconds + (60 * 60))
                .setSubject(credential.getServiceAccountId())
                .set("user_id", userId)
                .set("claims", "moo")
                .set("scope", "https://www.googleapis.com/auth/identitytoolkit");

        try {
            return JsonWebSignature.signUsingRsaSha256(privateKey, mFactory, header, payload);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "signUsingRsa:GeneralSecurityException", e);
        } catch (IOException e) {
            Log.e(TAG, "signUsingRsa:IOException", e);
        }

        return null;
    }

    private GoogleCredential readJsonFile() {
        // NOTE: This application includes the service account credentials as an Android raw
        // resource, however you should never do this in a real application! Any user could extract
        // these credentials and act on behalf of your application.
        InputStream is = mContext.getResources().openRawResource(R.raw.service_account);

        try {
            return GoogleCredential.fromStream(is, mTransport, mFactory);
        } catch (IOException e) {
            Log.e(TAG, "GoogleClientSecrets", e);
            return null;
        }
    }

}
