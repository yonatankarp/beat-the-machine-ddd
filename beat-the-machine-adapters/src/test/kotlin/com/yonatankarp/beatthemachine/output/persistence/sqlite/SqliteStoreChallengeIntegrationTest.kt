package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val SqliteStoreChallengeITSuite by testSuite {
    test("stores a fresh challenge and bumps the version") {
        val (storeChallenge) = newSqliteAdapters()
        val challenge = mediumChallenge(prompt = "pixel art cat".asPrompt())
        val saved = storeChallenge handle StoreChallenge.Command(challenge)
        saved.version shouldBe 1L
    }

    test("allows sequential stores with updated versions") {
        val (storeChallenge) = newSqliteAdapters()
        val challenge = mediumChallenge(prompt = "sequential".asPrompt())
        val v1 = storeChallenge handle StoreChallenge.Command(challenge)
        val v2 = storeChallenge handle StoreChallenge.Command(v1)
        v1.version shouldBe 1L
        v2.version shouldBe 2L
    }

    test("rejects a stale version on second store") {
        val (storeChallenge) = newSqliteAdapters()
        val c = mediumChallenge()
        storeChallenge handle StoreChallenge.Command(c)
        shouldThrow<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(c) }
    }

    test("persists all difficulty levels") {
        val (storeChallenge, findChallengeById) = newSqliteAdapters()
        Difficulty.entries.forEach { diff ->
            val c = Challenge.start("test prompt".asPrompt(), 2.lives(), difficulty = diff)
            storeChallenge handle StoreChallenge.Command(c)
            val found = findChallengeById answer FindChallengeById.Query(c.id)
            found.shouldNotBeNull()
            found.difficulty shouldBe diff
        }
    }

    test("persists picture states correctly") {
        val (storeChallenge, findChallengeById) = newSqliteAdapters()
        val pending = mediumChallenge(prompt = "pending pic".asPrompt())
        val ready = mediumChallenge(prompt = "ready pic".asPrompt(), picture = readyPicture())
        val failed = mediumChallenge(prompt = "failed pic".asPrompt(), picture = failedPicture())
        storeChallenge handle StoreChallenge.Command(pending)
        storeChallenge handle StoreChallenge.Command(ready)
        storeChallenge handle StoreChallenge.Command(failed)
        (findChallengeById answer FindChallengeById.Query(pending.id))?.picture shouldBe Picture.Pending
        (findChallengeById answer FindChallengeById.Query(ready.id))?.picture shouldBe Picture.Ready("https://example.com/img.png")
        (findChallengeById answer FindChallengeById.Query(failed.id))?.picture shouldBe Picture.Failed
    }
}
