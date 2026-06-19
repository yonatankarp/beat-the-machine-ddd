package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChallengeTemplateTest {
    @Test
    fun `carries difficulty, prompt and picture url`() {
        val t = ChallengeTemplate("id-1", Difficulty.EASY, Prompt("red car"), "/images/abc")
        assertEquals(Difficulty.EASY, t.difficulty)
        assertEquals(Prompt("red car"), t.prompt)
        assertEquals("/images/abc", t.pictureUrl)
    }
}
