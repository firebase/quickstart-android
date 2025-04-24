package com.google.firebase.example.dataconnect.gradle

import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask
import com.google.firebase.example.dataconnect.gradle.tasks.ExtractArchiveTask
import com.google.firebase.example.dataconnect.gradle.tasks.GenerateDataConnectSourcesTask
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
import com.google.firebase.example.dataconnect.gradle.util.NodeJsPaths
import com.google.firebase.example.dataconnect.gradle.util.OperatingSystem
import com.google.firebase.example.dataconnect.gradle.util.nodeJsPaths
import com.google.firebase.example.dataconnect.gradle.util.operatingSystem
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider

internal class DataConnectTasksConfigurer(
    dataConnectExtension: DataConnectExtension,
    private val downloadNodeJsArchiveTask: TaskProvider<DownloadNodeJsBinaryDistributionArchiveTask>,
    private val extractNodeJsArchiveTask: TaskProvider<ExtractArchiveTask>,
    private val setupFirebaseToolsTask: TaskProvider<SetupFirebaseToolsTask>,
    private val buildDirectory: Provider<Directory>,
    projectDirectory: Directory,
    providerFactory: ProviderFactory
) : () -> Unit {

    override fun invoke() {
        configureDownloadNodeJsArchiveTask()
        configureExtractNodeJsArchiveTask()
        configureSetupFirebaseToolsTask()
    }

    private val nodeJsVersion: Provider<String> =
        providerFactory.provider {
            dataConnectExtension.nodeJsVersion
                ?: throw GradleException(
                    "dataconnect.nodeJsVersion must be set in " +
                        "build.gradle or build.gradle.kts to " +
                        "specify the version of Node.js (https://nodejs.org) " +
                        "to install (e.g. \"20.9.0\") (error code 3acj27az2c)"
                )
        }

    private val operatingSystem: Provider<OperatingSystem> = providerFactory.operatingSystem()

    private val nodeJsPaths: Provider<NodeJsPaths> =
        providerFactory.nodeJsPaths(nodeJsVersion, operatingSystem)

    private val firebaseCliVersion: Provider<String> = providerFactory.provider {
        dataConnectExtension.firebaseCliVersion
            ?: throw GradleException(
                "dataconnect.firebaseCliVersion must be set in " +
                    "build.gradle or build.gradle.kts to " +
                    "specify the version of the Firebase CLI npm package " +
                    "(https://www.npmjs.com/package/firebase-tools) to use " +
                    "(e.g. \"13.25.0\") (error code xbmvkc3mtr)"
            )
    }

    private val nodeExecutable: Provider<RegularFile> = extractNodeJsArchiveTask.flatMap {
        providerFactory.zip(it.outputDirectory, nodeJsPaths) { outputDirectory, nodeJsPaths ->
            outputDirectory.file(nodeJsPaths.nodeExecutableRelativePath)
        }
    }

    private val npmExecutable: Provider<RegularFile> = extractNodeJsArchiveTask.flatMap {
        providerFactory.zip(it.outputDirectory, nodeJsPaths) { outputDirectory, nodeJsPaths ->
            outputDirectory.file(nodeJsPaths.npmExecutableRelativePath)
        }
    }

    private val dataConnectConfigDir: Provider<Directory> = providerFactory.provider {
        dataConnectExtension.dataConnectConfigDir?.let {
            projectDirectory.dir(it.path)
        }
    }

    private fun configureDownloadNodeJsArchiveTask() = downloadNodeJsArchiveTask.configure {
        group = TASK_GROUP

        archiveUrl.set(nodeJsPaths.map { it.archiveUrl })
        shasumsUrl.set(nodeJsPaths.map { it.shasumsUrl })

        archiveFile.set(
            providerFactory.zip(buildDirectory, nodeJsPaths) { buildDirectory, nodeJsPaths ->
                buildDirectory.file(nodeJsPaths.archiveFileName)
            }
        )

        shasumsFile.set(
            providerFactory.zip(buildDirectory, nodeJsPaths) { buildDirectory, nodeJsPaths ->
                buildDirectory.file(nodeJsPaths.shasumsFileName)
            }
        )
    }

    private fun configureExtractNodeJsArchiveTask() = extractNodeJsArchiveTask.configure {
        group = TASK_GROUP

        pathPrefixComponentStripCount.set(1)
        archiveFile.set(downloadNodeJsArchiveTask.flatMap { it.archiveFile })

        outputDirectory.set(
            providerFactory.zip(buildDirectory, nodeJsPaths) { buildDirectory, nodeJsPaths ->
                buildDirectory.dir(nodeJsPaths.archiveBaseFileName)
            }
        )
    }

    private fun configureSetupFirebaseToolsTask() = setupFirebaseToolsTask.configure {
        group = TASK_GROUP

        firebaseCliVersion.set(this@DataConnectTasksConfigurer.firebaseCliVersion)
        outputDirectory.set(buildDirectory.map { it.dir("firebase-tools") })

        nodeExecutable.set(this@DataConnectTasksConfigurer.nodeExecutable)
        npmExecutable.set(this@DataConnectTasksConfigurer.npmExecutable)
    }

    fun configureGenerateDataConnectSourcesTask(
        task: TaskProvider<GenerateDataConnectSourcesTask>,
        variantName: String
    ) = task.configure {
        group = TASK_GROUP

        @Suppress("ktlint:standard:max-line-length")
        setOnlyIf(
            "dataconnect.dataConnectConfigDir is null; to enable the \"$name\" task, " +
                "set dataconnect.dataConnectConfigDir in build.gradle or build.gradle.kts to " +
                "the directory that defines the Data Connect schema and " +
                "connectors whose Kotlin code to generate code. That is, the directory " +
                "containing the dataconnect.yaml file. For details, see " +
                "https://firebase.google.com/docs/data-connect/configuration-reference#dataconnect.yaml-configuration " +
                "(e.g. file(\"../dataconnect\")) (message code a3ch245mbd)"
        ) { dataConnectConfigDir.isPresent }

        dataConnectConfigDir.set(this@DataConnectTasksConfigurer.dataConnectConfigDir)
        firebaseExecutable.set(setupFirebaseToolsTask.map { it.firebaseExecutable })
        nodeExecutable.set(this@DataConnectTasksConfigurer.nodeExecutable)
        tweakedDataConnectConfigDir.set(buildDirectory.map { it.dir("variants/$variantName/config") })
    }
}

private const val TASK_GROUP = "Firebase Data Connect"
