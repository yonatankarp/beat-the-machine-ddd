package com.yonatankarp.beatthemachine.test.fixtures

import com.yonatankarp.beatthemachine.domain.valueobject.Picture

object Pictures {
    fun pendingPicture(): Picture = Picture.Pending

    fun readyPicture(url: String = "https://example.com/img.png"): Picture = Picture.Ready(url)

    fun failedPicture(): Picture = Picture.Failed
}
