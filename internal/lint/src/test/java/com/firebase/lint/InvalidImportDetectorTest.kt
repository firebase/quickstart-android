package com.firebase.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.firebase.lint.InvalidImportDetector.Companion.SHORT_MESSAGE
import org.junit.Test

class InvalidImportDetectorTest {

    private val javaPackage = java("""
      package com.google.firebase.java;

      public final class Hello {
        public static final class drawable {
        }
      }""").indented()

    @Test
    fun normalRImport() {
        lint()
                .files(javaPackage, java("""
          package com.google.firebase.kotlin;

          import com.google.firebase.Hello;

          class Example {
          }""").indented())
                .issues(ISSUE_INVALID_IMPORT)
                .run()
                .expectClean()
    }

    @Test
    fun wrongImport() {
        lint()
                .files(javaPackage, java("""
          package com.google.firebase.kotlin;

          import com.google.firebase.java.Hello;

          class Example {
          }""").indented())
                .issues(ISSUE_INVALID_IMPORT)
                .run()
                .expect("""
          |src/com/google/firebase/kotlin/Example.java:3: Error: $SHORT_MESSAGE [SuspiciousImport]
          |import com.google.firebase.java.Hello;
          |       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
    }
}
