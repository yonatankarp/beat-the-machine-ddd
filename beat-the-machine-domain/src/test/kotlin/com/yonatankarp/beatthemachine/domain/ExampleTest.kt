package com.yonatankarp.beatthemachine.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ExampleTest {
    @Test
    fun `should return value`() {
        val example = Example("Hello world!")
        example.value shouldBe "Hello world!"
    }

    @Test
    fun `should return true when value is blank`() {
        val example = Example("   ")
        example.isEmpty() shouldBe true
    }

    @Test
    fun `should return false when value is not blank`() {
        val example = Example("Hello")
        example.isEmpty() shouldBe false
    }

    @Test
    fun `should convert value to uppercase`() {
        val example = Example("hello world")
        example.toUpperCase() shouldBe "HELLO WORLD"
    }
}
