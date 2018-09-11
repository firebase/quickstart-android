package com.firebase.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UImportStatement


const val PRIORITY = 10

val ISSUE_INVALID_IMPORT = Issue.create(
        "SuspiciousImport", //$NON-NLS-1$
        "importing files from the `java` package in a kotlin file",
        "Importing files from the java package is usually not intentional; it sometimes happens when " +
                "you have classes with the same name in both `java` and `kotlin` package.",
        Category.CORRECTNESS,
        9,
        Severity.WARNING,
        Implementation(
                InvalidImportDetector::class.java,
                Scope.JAVA_FILE_SCOPE))

private const val disallowedImports = ".java."

class InvalidImportDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext) = InvalidImportHandler(context)

    class InvalidImportHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitImportStatement(node: UImportStatement) {

            node.importReference?.let { importReference ->

                if (importReference.asSourceString().contains(disallowedImports)) {
                    context.report(ISSUE_INVALID_IMPORT, node, context.getLocation(importReference), "Invalid import")
                }

            }
        }
    }
}