package com.firebase.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class ImportIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues: List<Issue>
        get() = listOf(ISSUE_INVALID_IMPORT)

}