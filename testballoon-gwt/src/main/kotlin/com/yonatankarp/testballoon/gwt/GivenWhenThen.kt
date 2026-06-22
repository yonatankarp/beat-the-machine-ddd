package com.yonatankarp.testballoon.gwt

import de.infix.testBalloon.framework.core.Test
import de.infix.testBalloon.framework.core.TestSuiteScope
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class GivenWhenThenScope(
    internal val testSuiteScope: TestSuiteScope,
)

class ThenScope(
    internal val testSuiteScope: TestSuiteScope,
)

class ScenarioValue<T>(
    private val value: () -> T,
) : ReadOnlyProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T {
        val context =
            currentScenarioContext.get()
                ?: error("Scenario value '${property.name}' was read outside a then block")

        @Suppress("UNCHECKED_CAST")
        return context.values.getOrPut(this) { value() } as T
    }
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
    content: ThenScope.() -> Unit,
): Unit =
    with(testSuiteScope) {
        testSuite("when $description") {
            ThenScope(this).content()
        }
    }

fun ThenScope.then(
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

fun <T> GivenWhenThenScope.setup(value: () -> T): ScenarioValue<T> = ScenarioValue(value)

fun <T> ThenScope.setup(value: () -> T): ScenarioValue<T> = ScenarioValue(value)

fun <T> ThenScope.action(value: () -> T): ScenarioValue<T> = ScenarioValue(value)
