package com.yonatankarp.beatthemachine.config

import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PictureScopeTest {
    @Test
    fun `is active before shutdown`() {
        val scope = PictureScope()
        assertTrue(scope.isActive)
    }

    @Test
    fun `destroy cancels the scope so no work outlives the application`() {
        val scope = PictureScope()

        scope.destroy()

        assertFalse(scope.isActive)
        assertTrue(scope.coroutineContext.job.isCancelled)
    }
}
