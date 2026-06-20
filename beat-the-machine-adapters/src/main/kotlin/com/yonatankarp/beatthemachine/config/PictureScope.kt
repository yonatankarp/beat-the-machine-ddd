package com.yonatankarp.beatthemachine.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.springframework.beans.factory.DisposableBean
import kotlin.coroutines.CoroutineContext

class PictureScope :
    CoroutineScope,
    DisposableBean {
    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.IO.limitedParallelism(PICTURE_PARALLELISM)

    override fun destroy() {
        cancel("Application shutting down")
    }

    companion object {
        const val PICTURE_PARALLELISM = 4
    }
}
