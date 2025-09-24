package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.riddle.Riddle

/**
 * Output port for finding available riddles.
 */
fun interface FindAvailableRiddles {
    operator fun invoke(): List<Riddle>
}
