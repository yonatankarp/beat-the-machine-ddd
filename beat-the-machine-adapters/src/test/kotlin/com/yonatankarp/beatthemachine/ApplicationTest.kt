package com.yonatankarp.beatthemachine

import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationContext : SpringTestConfig()

val ApplicationSuite by testSuite {
    springTest<ApplicationContext> {
        test("context loads") {}
    }
}
