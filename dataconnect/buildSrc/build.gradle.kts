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

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin.api)
  implementation(gradleKotlinDsl())
}

gradlePlugin {
  plugins {
    create("dataconnect") {
      id = "com.google.firebase.example.dataconnect.gradle"
      implementationClass = "com.google.firebase.example.dataconnect.gradle.DataConnectGradlePlugin"
    }
  }
}
