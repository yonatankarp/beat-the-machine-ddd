package com.yonatankarp.beatthemachine.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class FavIconController {
    @GetMapping("favicon.ico")
    @ResponseBody
    fun favicon() {
        // Controller that returns no value to avoid the 404 error thrown by
        // the browser as described in: https://www.javadevjournal.com/spring-boot/spring-boot-favicon/
    }
}
