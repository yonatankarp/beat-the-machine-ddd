package com.yonatankarp.beatthemachine.config

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

val PictureScopeSuite by testSuite {
    test("is active before shutdown") {
        val scope = PictureScope()

        scope.isActive.shouldBeTrue()
    }

    test("destroy cancels the scope so no work outlives the application") {
        val scope = PictureScope()

        scope.destroy()

        scope.isActive.shouldBeFalse()
        scope.coroutineContext.job.isCancelled
            .shouldBeTrue()
    }
}
