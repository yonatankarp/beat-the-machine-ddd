package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.riddle.Riddle

/**
 * Output port for finding available riddles.
 */
fun interface FindAvailableRiddles {
    /**
 * Retrieve the currently available riddles.
 *
 * Implementations should return a list of Riddle objects that are available to be attempted; the list
 * may be empty if no riddles are available.
 *
 * @return a list of available [Riddle] instances.
 */
operator fun invoke(): List<Riddle>
}
