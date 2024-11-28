/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.example.dataconnect.gradle

import java.io.File

interface DataConnectExtension {

    /**
     * The version of Node.js (https://nodejs.org) to use to install and run the
     * Firebase CLI. This version of Node.js will be downloaded and extracted
     * for exclusive use of the Data Connect Gradle plugin.
     */
    var nodeVersion: String?

    /**
     * The version of the Firebase CLI (https://www.npmjs.com/package/firebase-tools)
     * to use to perform the Data Connect Kotlin code generation.
     */
    var firebaseCliVersion: String?

    /**
     * The directory that contains dataconnect.yaml that specifies the Data
     * Connect schema and connectors whose code to generate. If the file is a
     * relative directory (as opposed to an absolute directory) then it will be
     * calculated relative to the evaluating project's directory.
     *
     * If this value is null then no Data Connect code generation will occur
     * as part of the build.
     */
    var dataConnectConfigDir: File?
}
