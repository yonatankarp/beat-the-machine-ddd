package com.yonatankarp.beatthemachine.config

import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PictureScopeTest {
    @Test
    fun `is active before shutdown`() {
        // Given
        val scope = PictureScope()

        // When
        val active = scope.isActive

        // Then
        assertTrue(active)
    }

    @Test
    fun `destroy cancels the scope so no work outlives the application`() {
        // Given
        val scope = PictureScope()

        // When
        scope.destroy()

        // Then
        assertFalse(scope.isActive)
        assertTrue(scope.coroutineContext.job.isCancelled)
    }
}
