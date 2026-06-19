package com.yonatankarp.beatthemachine.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.springframework.beans.factory.DisposableBean
import kotlin.coroutines.CoroutineContext

/**
 * Application-wide scope for fire-and-forget picture generation.
 *
 * Replaces the old `core 2 / max 4` thread pool with structured concurrency:
 *  - a [SupervisorJob] so one failed generation never cancels its siblings or the scope,
 *  - [Dispatchers.IO] limited to [PICTURE_PARALLELISM] threads, bounding how many picture
 *    coroutines run on this scope's dispatcher at once.
 *
 * Note this caps only time spent *on this dispatcher*: each picture coroutine still hops to
 * the shared (unbounded) `Dispatchers.IO` pool inside the JDBC adapters' `withContext`, so
 * concurrent SQL statements are not limited to [PICTURE_PARALLELISM] here — the connection
 * pool (Hikari) is the real database-concurrency limiter. Backlog size is bounded separately
 * by the admission semaphore in [com.yonatankarp.beatthemachine.output.ai.PicturePregeneration].
 *
 * Cancelled on context shutdown via [DisposableBean], so in-flight generations are
 * cancelled and no coroutine outlives the application.
 */
class PictureScope :
    CoroutineScope,
    DisposableBean {
    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.IO.limitedParallelism(PICTURE_PARALLELISM)

    override fun destroy() {
        cancel("Application shutting down")
    }

    companion object {
        /** Matches the former thread pool's max parallelism. */
        const val PICTURE_PARALLELISM = 4
    }
}
