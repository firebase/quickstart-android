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

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import java.util.Locale
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register

@Suppress("unused")
abstract class DataConnectGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("dataconnect", DataConnectExtension::class.java)
        val providers = project.objects.newInstance<MyProjectProviders>()

        project.tasks.register<FirebaseToolsSetupTask>("setupFirebaseToolsForDataConnect") {
            configureFrom(providers)
        }

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants { variant ->
            val variantProviders = project.objects.newInstance<MyVariantProviders>(variant, providers)
            registerVariantTasks(project, variant, variantProviders)
        }
    }

    private fun registerVariantTasks(
        project: Project,
        variant: ApplicationVariant,
        providers: MyVariantProviders
    ) {
        val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }

        val generateCodeTask =
            project.tasks.register<CodegenTask>("generate${variantNameTitleCase}DataConnectSources") {
                configureFrom(providers)
            }

        variant.sources.java!!.addGeneratedSourceDirectory(
            generateCodeTask,
            CodegenTask::outputDirectory
        )
    }
}
