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
  // See https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
  `kotlin-dsl`
  kotlin("plugin.serialization") version embeddedKotlinVersion
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

dependencies {
  implementation(libs.android.gradlePlugin.api)
  implementation(libs.snakeyaml)

  val ktorVersion = "2.3.13"
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")

  implementation("org.pgpainless:pgpainless-sop:1.7.2")
  implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
  implementation("org.apache.commons:commons-compress:1.27.1")
}

gradlePlugin {
  plugins {
    create("dataconnect") {
      id = "com.google.firebase.example.dataconnect.gradle"
      implementationClass = "com.google.firebase.example.dataconnect.gradle.DataConnectGradlePlugin"
    }
  }
}
