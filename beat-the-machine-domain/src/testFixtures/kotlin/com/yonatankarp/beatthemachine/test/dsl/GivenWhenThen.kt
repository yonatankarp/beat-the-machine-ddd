package com.yonatankarp.beatthemachine.test.dsl

import de.infix.testBalloon.framework.core.Test
import de.infix.testBalloon.framework.core.TestSuiteScope

fun TestSuiteScope.given(
    description: String,
    content: TestSuiteScope.() -> Unit,
): Unit = testSuite("given $description") { content() }

fun TestSuiteScope.whenever(
    description: String,
    content: TestSuiteScope.() -> Unit,
): Unit = testSuite("when $description") { content() }

fun TestSuiteScope.then(
    description: String,
    action: suspend Test.ExecutionScope.() -> Unit,
): Unit = test("then $description") { action() }
