package com.yonatankarp.beatthemachine.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Tuning for the local Stable Diffusion (Automatic1111) image path. Bound from the
 * `btm.image.local-sd.*` properties; defaults mirror the values in application.yml so
 * the object is usable on its own. The HTTP transport itself is owned by the adapter,
 * not described here.
 */
@ConfigurationProperties(prefix = "btm.image.local-sd")
data class LocalStableDiffusionProperties(
    val baseUrl: String = "http://localhost:7860",
    val steps: Int = 8,
    val width: Int = 512,
    val height: Int = 512,
    val timeoutSeconds: Long = 120,
)
