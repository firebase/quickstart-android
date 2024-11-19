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
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class LocalConfig(val npmExecutable: String? = null, @Transient val srcFile: File? = null) :
    java.io.Serializable {
    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 6103369922496556758L
    }
}
