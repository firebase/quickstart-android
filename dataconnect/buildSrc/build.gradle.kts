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

  // TODO: Upgrade the `tomlkt` dependency to 0.4.0 or later once the gradle
  //  wrapper version used by this project uses a sufficiently-recent version
  //  of kotlin. At the time of writing, `embeddedKotlinVersion` is 1.9.22,
  //  which requires an older version of `tomlkt` because the newer versions
  //  depend on a newer version of the `kotlinx.serialization` plugin, which
  //  requires a newer version of Kotlin.
  implementation("net.peanuuutz.tomlkt:tomlkt:0.3.7")

  val ktorVersion = "2.3.12"
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
}

gradlePlugin {
  plugins {
    create("dataconnect") {
      id = "com.google.firebase.example.dataconnect.gradle"
      implementationClass = "com.google.firebase.example.dataconnect.gradle.DataConnectGradlePlugin"
    }
  }
}
