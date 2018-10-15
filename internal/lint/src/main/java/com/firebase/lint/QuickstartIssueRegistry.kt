package com.firebase.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("unused")
class QuickstartIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues: List<Issue>
        get() = listOf(ISSUE_INVALID_IMPORT, ISSUE_HUNGARIAN_NOTATION)
}
