package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeStore
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindChallengeById
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindPendingChallenges
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryStoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
val PicturePregenerationSuite by testSuite {
    given("a picture pregeneration pipeline") {
        whenever("enqueueing a pending challenge with a working machine") {
            then("generation flips a pending picture to ready and persists it") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val machine = mockk<Machine>().also { coEvery { it answer any() } returns Picture.Ready("http://img/1.png") }
                    val c = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                    pregeneration(machine, storeChallenge, findById, findPending, this).enqueue(c.id)
                    advanceUntilIdle()
                    (findById answer FindChallengeById.Query(c.id))?.picture shouldBe Picture.Ready("http://img/1.png")
                }
            }
        }

        whenever("the machine fails") {
            then("it persists a failed picture") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val machine = mockk<Machine>().also { coEvery { it answer any() } throws RuntimeException("boom") }
                    val c = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                    pregeneration(machine, storeChallenge, findById, findPending, this).enqueue(c.id)
                    advanceUntilIdle()
                    (findById answer FindChallengeById.Query(c.id))?.picture shouldBe Picture.Failed
                }
            }
        }

        whenever("enqueueing an unknown challenge") {
            then("it is a no-op") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val machine = mockk<Machine>().also { coEvery { it answer any() } returns Picture.Ready("http://img/1.png") }
                    pregeneration(machine, storeChallenge, findById, findPending, this).enqueue(aChallengeId())
                    advanceUntilIdle()
                }
            }
        }

        whenever("retrying pending challenges") {
            then("it re-enqueues every challenge still awaiting a picture") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val machine = mockk<Machine>().also { coEvery { it answer any() } returns Picture.Ready("http://img/retry.png") }
                    val pending = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                    val done =
                        storeChallenge handle
                            StoreChallenge.Command(
                                mediumChallenge(prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/done.png")),
                            )
                    pregeneration(machine, storeChallenge, findById, findPending, this).retryPending()
                    advanceUntilIdle()
                    (findById answer FindChallengeById.Query(pending.id))?.picture shouldBe Picture.Ready("http://img/retry.png")
                    (findById answer FindChallengeById.Query(done.id))?.picture shouldBe Picture.Ready("http://img/done.png")
                }
            }
        }

        whenever("a concurrent version bump conflicts during persistence") {
            then("it is retried so the picture still persists") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val seeded = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                    var firstStore = true
                    val racingStore =
                        object : StoreChallenge {
                            override suspend fun handle(command: StoreChallenge.Command): Challenge {
                                val challenge = command.challenge
                                if (firstStore && challenge.picture is Picture.Ready) {
                                    firstStore = false
                                    storeChallenge handle
                                        StoreChallenge.Command(findById answer FindChallengeById.Query(challenge.id) ?: challenge)
                                    throw OptimisticLockConflict(challenge.id)
                                }
                                return storeChallenge handle StoreChallenge.Command(challenge)
                            }
                        }
                    val machine = mockk<Machine>().also { coEvery { it answer any() } returns Picture.Ready("http://img/raced.png") }
                    pregeneration(machine, racingStore, findById, findPending, this).enqueue(seeded.id)
                    advanceUntilIdle()
                    (findById answer FindChallengeById.Query(seeded.id))?.picture shouldBe Picture.Ready("http://img/raced.png")
                }
            }
        }

        whenever("the admission bound is full") {
            then("enqueue sheds work") {
                runTest {
                    val store = InMemoryChallengeStore()
                    val storeChallenge = InMemoryStoreChallenge(store)
                    val findById = InMemoryFindChallengeById(store)
                    val findPending = InMemoryFindPendingChallenges(store)
                    val machine = mockk<Machine>().also { coEvery { it answer any() } returns Picture.Ready("http://img/admitted.png") }
                    val admitted = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                    val shed = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "foo bar".asPrompt()))
                    val pregen = pregeneration(machine, storeChallenge, findById, findPending, this, maxQueued = 1)
                    pregen.enqueue(admitted.id)
                    pregen.enqueue(shed.id)
                    advanceUntilIdle()
                    (findById answer FindChallengeById.Query(admitted.id))?.picture shouldBe Picture.Ready("http://img/admitted.png")
                    (findById answer FindChallengeById.Query(shed.id))?.picture shouldBe Picture.Pending
                }
            }
        }
    }
}

private fun pregeneration(
    machine: Machine,
    store: StoreChallenge,
    findById: InMemoryFindChallengeById,
    findPending: InMemoryFindPendingChallenges,
    scope: CoroutineScope,
    maxQueued: Int = 50,
) = PicturePregeneration(machine, findById, store, findPending, scope, maxQueued)
