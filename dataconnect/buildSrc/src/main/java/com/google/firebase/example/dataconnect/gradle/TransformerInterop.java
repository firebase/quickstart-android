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
package com.google.firebase.example.dataconnect.gradle;

import org.gradle.api.Transformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: Remove this interface and use Transformer directly once the Kotlin
//  version is upgraded to a later version that doesn't require it, such as
//  1.9.25. At the time of writing, the Kotlin version in use is 1.9.22.
//
// Using this interface works around the following Kotlin compiler error:
//
// > Task :plugin:compileKotlin FAILED
// e: DataConnectGradlePlugin.kt:93:15 Type mismatch: inferred type is RegularFile? but TypeVariable(S) was expected
// e: DataConnectGradlePlugin.kt:102:15 Type mismatch: inferred type is String? but TypeVariable(S) was expected
// e: DataConnectGradlePlugin.kt:111:15 Type mismatch: inferred type is DataConnectExecutable.VerificationInfo? but TypeVariable(S) was expected
public interface TransformerInterop<OUT, IN> extends Transformer<OUT, IN> {

    @Override
    @Nullable OUT transform(@NotNull IN in);

}