package com.yonatankarp.beatthemachine.controllers

import com.yonatankarp.beatthemachine.models.GuessRequest
import com.yonatankarp.beatthemachine.models.GuessResponse
import com.yonatankarp.beatthemachine.services.RiddleManager
import com.yonatankarp.beatthemachine.services.RiddleService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RiddleController(
    val riddleService: RiddleService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(RiddleController::class.java)
    }

    @GetMapping(value = ["/", "index", "index.html"])
    fun index(model: Model): String {
        log.info("Loading index...")
        riddleService.getRandomRiddle().let {
            model.addAttribute("guess", GuessResponse(emptyList()))
            model.addAttribute("riddle", it)
            model.addAttribute("response", it.initPrompt())
        }
        return "index"
    }

    @PostMapping(value = ["/{id}/i-give-up"])
    fun iGiveUp(
        @PathVariable id: Int,
        model: Model,
    ): String {
        log.info("Giving up on riddle id: $id")
        riddleService.getRiddle(id.toRiddleId()).let {
            model.addAttribute("guess", GuessResponse(listOf()))
            model.addAttribute("riddle", it)
            model.addAttribute("response", it.giveUp())
        }

        return "game"
    }

    @PostMapping("/{id}/guess")
    fun submitGuess(
        @PathVariable id: Int,
        @ModelAttribute guess: GuessRequest,
        model: Model,
    ): String {
        val riddleId = id.toRiddleId()
        log.info("Guess for id $riddleId is ${guess.words}")
        riddleService
            .handleGuess(riddleId, guess)
            .let {
                model.addAttribute("riddle", RiddleManager.riddles[riddleId])
                model.addAttribute("guess", guess)
                model.addAttribute("response", it)
            }
        return "game"
    }

    private fun Int.toRiddleId() = this % (RiddleManager.numberOfRiddles)
}
