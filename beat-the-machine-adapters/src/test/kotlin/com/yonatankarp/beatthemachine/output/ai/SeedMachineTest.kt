package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SeedMachineTest {
    private val machine = SeedMachine()

    @Test
    fun `returns the curated url for a known prompt`() =
        runTest {
            // Given
            val (prompt, url) = SEED.first()

            // When
            val result = machine.generate(prompt)

            // Then
            assertEquals(Picture.Ready(url), result)
        }

    @Test
    fun `returns Failed for an unknown prompt`() =
        runTest {
            // Given
            val prompt = Prompt("a prompt that is not seeded")

            // When
            val result = machine.generate(prompt)

            // Then
            assertEquals(Picture.Failed, result)
        }
}
