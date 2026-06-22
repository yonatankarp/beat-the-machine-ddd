package com.yonatankarp.testballoon.gwt

import de.infix.testBalloon.framework.core.Test
import de.infix.testBalloon.framework.core.TestSuiteScope
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class GivenWhenThenScope(
    internal val testSuiteScope: TestSuiteScope,
)

class WhenScope(
    internal val testSuiteScope: TestSuiteScope,
)

class ThenScope(
    internal val testSuiteScope: TestSuiteScope,
)

class ScenarioValue<T>(
    private val value: suspend () -> T,
) : ReadOnlyProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        val context =
            currentScenarioContext.get()
                ?: error("Scenario value '${property.name}' was read outside a then block")

        @Suppress("UNCHECKED_CAST")
        return context.values.getOrPut(this) { runSuspend(value, ScenarioContextElement(context)) } as T
    }
}

private fun <T> runSuspend(
    value: suspend () -> T,
    coroutineContext: CoroutineContext,
): T {
    val completed = CountDownLatch(1)
    val resultHolder = AtomicReference<Result<T>>()
    val wrapped: suspend () -> T = { withContext(coroutineContext) { value() } }
    wrapped.startCoroutine(
        object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                resultHolder.set(result)
                completed.countDown()
            }
        },
    )
    completed.await()
    return resultHolder.get().getOrThrow()
}

private class ScenarioContext {
    val values: MutableMap<ScenarioValue<*>, Any?> = mutableMapOf()
}

private class ScenarioContextElement(
    private val context: ScenarioContext,
) : ThreadContextElement<ScenarioContext?> {
    companion object Key : CoroutineContext.Key<ScenarioContextElement>

    override val key: CoroutineContext.Key<ScenarioContextElement> = Key

    override fun updateThreadContext(context: CoroutineContext): ScenarioContext? {
        val previous = currentScenarioContext.get()
        currentScenarioContext.set(this.context)
        return previous
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: ScenarioContext?,
    ) {
        currentScenarioContext.set(oldState)
    }
}

private val currentScenarioContext = ThreadLocal<ScenarioContext?>()

fun TestSuiteScope.given(
    description: String,
    content: GivenWhenThenScope.() -> Unit,
): Unit =
    testSuite("given $description") {
        GivenWhenThenScope(this).content()
    }

fun GivenWhenThenScope.given(
    description: String,
    content: GivenWhenThenScope.() -> Unit,
): Unit =
    with(testSuiteScope) {
        testSuite("given $description") {
            GivenWhenThenScope(this).content()
        }
    }

fun GivenWhenThenScope.whenever(
    description: String,
    content: WhenScope.() -> Unit,
): Unit =
    with(testSuiteScope) {
        testSuite("when $description") {
            WhenScope(this).content()
        }
    }

fun WhenScope.then(
    description: String,
    action: suspend Test.ExecutionScope.() -> Unit,
): Unit =
    with(testSuiteScope) {
        test("then $description") {
            val executionScope = this
            withContext(ScenarioContextElement(ScenarioContext())) {
                executionScope.action()
            }
        }
    }

fun <T> GivenWhenThenScope.setup(value: suspend () -> T): ScenarioValue<T> = ScenarioValue(value)

fun <T> WhenScope.action(value: suspend () -> T): ScenarioValue<T> = ScenarioValue(value)
