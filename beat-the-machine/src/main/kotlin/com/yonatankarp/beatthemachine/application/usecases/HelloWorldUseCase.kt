package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.HelloWorldPort
import com.yonatankarp.beatthemachine.domain.valueobject.HelloWorld
import org.springframework.stereotype.Service

@Service
class HelloWorldUseCase : HelloWorldPort {
    override suspend fun greet() = HelloWorld().sayHello()
}
