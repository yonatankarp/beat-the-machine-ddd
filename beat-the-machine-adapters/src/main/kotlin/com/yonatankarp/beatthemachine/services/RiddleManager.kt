package com.yonatankarp.beatthemachine.services

import com.yonatankarp.beatthemachine.models.Riddle
import com.yonatankarp.beatthemachine.utils.toHiddenString

object RiddleManager {
    val riddles =
        listOf(
            "man stands on a man" to "https://s3.amazonaws.com/ai.protogenes/art/28b9da08-4282-11ed-8be2-ee31c059bf00.png",
            "dolphin on fire" to "https://s3.amazonaws.com/ai.protogenes/art/840e15ae-18bb-11ed-9f15-ba15d03b6eca.png",
            "astronaut eating the moon" to "https://s3.amazonaws.com/ai.protogenes/art/9d73d604-4189-11ed-8cdd-2e988650e75d.png",
            "the quiet before the storm" to "https://s3.amazonaws.com/ai.protogenes/art/13bb8906-418a-11ed-8cdd-2e988650e75d.png",
            "dragon eating a cookie" to "https://s3.amazonaws.com/ai.protogenes/art/847263c4-42af-11ed-b2cd-366e053c1cb8.png",
        ).mapIndexed { index, pair ->
            Riddle(
                id = index,
                startPrompt = pair.first.toHiddenString(),
                prompt = pair.first,
                url = pair.second,
            )
        }.toTypedArray()

    val numberOfRiddles: Int
        get() = riddles.size
}
