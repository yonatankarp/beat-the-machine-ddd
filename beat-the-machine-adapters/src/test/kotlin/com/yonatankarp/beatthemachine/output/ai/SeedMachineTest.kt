package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SeedMachineTest {
    private val machine = SeedMachine()

    @Test
    fun `returns the curated url for a known prompt`() {
        val (prompt, url) = SEED.first()
        assertEquals(Picture.Ready(url), machine.generate(prompt))
    }

    @Test
    fun `returns Failed for an unknown prompt`() {
        assertEquals(Picture.Failed, machine.generate(Prompt("a prompt that is not seeded")))
    }
}
