package com.yonatankarp.beatthemachine.config

import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

val PictureScopeSuite by testSuite {
    given("a picture scope") {
        whenever("the application is still running") {
            then("it is active before shutdown") {
                val scope = PictureScope()
                scope.isActive.shouldBeTrue()
            }
        }

        whenever("destroy is called") {
            then("it cancels the scope so no work outlives the application") {
                val scope = PictureScope()
                scope.destroy()
                scope.isActive.shouldBeFalse()
                scope.coroutineContext.job.isCancelled
                    .shouldBeTrue()
            }
        }
    }
}
