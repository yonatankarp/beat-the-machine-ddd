package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import java.util.concurrent.ConcurrentHashMap

class InMemoryPictureStorage {
    val byId = ConcurrentHashMap<String, StoredImage>()
}
