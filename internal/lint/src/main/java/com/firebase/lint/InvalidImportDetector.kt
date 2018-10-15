package com.firebase.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UImportStatement

val ISSUE_INVALID_IMPORT = Issue.create(
        "SuspiciousImport",
        "importing files from the `java` package in a kotlin file",
        "Importing files from the java package is usually not intentional; it sometimes happens when " +
                "you have classes with the same name in both `java` and `kotlin` package.",
        Category.CORRECTNESS,
        9,
        Severity.ERROR,
        Implementation(
                InvalidImportDetector::class.java,
                Scope.JAVA_FILE_SCOPE))

class InvalidImportDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext) = InvalidImportHandler(context)

    class InvalidImportHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitImportStatement(node: UImportStatement) {
            var importedPackageName = ""
            val classPackageName = context.uastFile?.packageName.toString()

            node.importReference?.let {
                importedPackageName = it.asSourceString()
            }

            val classPackageSubFolders = classPackageName.split(".")
            val importedPackageSubFolders = importedPackageName.split(".")

            var i = 0
            while (i < classPackageSubFolders.size && i < importedPackageSubFolders.size) {
                if (classPackageSubFolders[i] == "kotlin" && importedPackageSubFolders[i] == "java") {
                    node.importReference?.let {
                        context.report(ISSUE_INVALID_IMPORT, node, context.getLocation(it), SHORT_MESSAGE)
                    }
                }
                i++
            }
        }
    }

    companion object {
        const val SHORT_MESSAGE = "Invalid Import: java package imported from kotlin package."
    }
}
