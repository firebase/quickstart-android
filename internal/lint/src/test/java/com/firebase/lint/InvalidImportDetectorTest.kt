package com.firebase.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class InvalidImportDetectorTest {

    private val javaPackage = java("""
      |package com.google.firebase.java;
      |
      |public final class Hello {
      |  public static final class drawable {
      |  }
      |}""".trimMargin())

    @Test
    fun normalRImport() {
        lint()
                .files(javaPackage, java("""
          |package com.google.firebase.kotlin;
          |
          |import com.google.firebase.Hello;
          |
          |class Example {
          |}""".trimMargin()))
                .issues(ISSUE_INVALID_IMPORT)
                .run()
                .expectClean()
    }

    @Test
    fun wrongImport() {
        lint()
                .files(javaPackage, java("""
          |package com.google.firebase.kotlin;
          |
          |import com.google.firebase.java.Hello;
          |
          |class Example {
          |}""".trimMargin()))
                .issues(ISSUE_INVALID_IMPORT)
                .run()
                .expect("""
          |src/com/google/firebase/kotlin/Example.java:3: Warning: Invalid import [SuspiciousImport]
          |import com.google.firebase.java.Hello;
          |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin())
    }
}
