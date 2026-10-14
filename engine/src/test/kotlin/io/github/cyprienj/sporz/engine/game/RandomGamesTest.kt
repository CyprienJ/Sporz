package io.github.cyprienj.sporz.engine.game

import io.github.cyprienj.sporz.engine.checkVictory
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.night.hackableSteps
import io.github.cyprienj.sporz.engine.night.healCount
import io.github.cyprienj.sporz.engine.night.paralysable
import io.github.cyprienj.sporz.engine.setup.Seat
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * Plays many seeded games with random legal moves and checks invariants after
 * every command, to find crashes and rule breaks the scripted tests miss.
 * A failing seed is printed and replays identically.
 */
class RandomGamesTest {

    private val variantSets = listOf(
        Variants(),
        Variants(captain = true),
        Variants(hackerCanHackSpy = true, blankVoteAllowed = false),
        Variants(captain = true, revealOnElimination = false, hackerCanHackSpy = true),
    )

    @Test
    fun `400 random games keep every invariant`() {
        var finished = 0
        repeat(400) { seed ->
            val random = Random(seed)
            val playerCount = random.nextInt(7, 21)
            val variants = variantSets[seed % variantSets.size]
            val seats = (0 until playerCount).map { Seat(PlayerId("p$it"), "Player $it") }
            val suggested = SuggestedCompositions.forPlayers(playerCount, variants)
            // Half the games also have the informant and the investigator, when seats are left.
            val composition = if (seed % 2 == 1 && playerCount - suggested.chosenRoleCount >= 2) {
                suggested.copy(roles = suggested.roles + (Role.Informant to 1) + (Role.Investigator to 1))
            } else suggested
            var game = GameState.start(seats, composition, GameSettings(), random)
            val dealt = game.players

            var commands = 0
            while (game.phase != Phase.Ended && game.round <= 40) {
                val before = game
                game = runCatching { next(game, random) }.getOrElse { throw AssertionError("seed $seed, round ${before.round}: ${it.message}", it) }
                check(seed, before, game, dealt.associate { it.id to (it.role to it.genome) })
                if (++commands % 7 == 0) assertEquals(game, GameState.fromJson(game.toJson()), "seed $seed: save/restore")
            }
            if (game.phase == Phase.Ended) finished++
        }
        // Random votes are often blank or split, but most games must still end.
        assertTrue(finished > 300, "only $finished games out of 400 ended within 40 rounds")
    }

    /** One random legal command for the current state. */
    private fun next(game: GameState, random: Random): GameState {
        val alive = game.players.filter { it.alive }
        fun someone(except: PlayerId? = null) = alive.filter { it.id != except }.random(random).id

        if (game.phase == Phase.Night) {
            val call = game.night!!.progress.current!!
            if (call.pretend) return game.pretendStep()
            val actors = call.actors
            return when (game.currentStep!!) {
                NightStep.Mutants -> {
                    val healthy = alive.filter { !it.isMutated }
                    val target = healthy.random(random).id
                    val paralysed = paralysable(game.players, target).randomOrNull(random)
                    game.playMutants(if (random.nextBoolean()) MutantsDecision.Mutate(target, paralysed) else MutantsDecision.Kill(target, paralysed))
                }
                NightStep.Doctors -> {
                    val others = alive.filter { it.id !in actors }
                    if (others.isNotEmpty() && random.nextInt(4) == 0) {
                        game.playDoctors(DoctorsDecision.Kill(others.random(random).id))
                    } else {
                        game.playDoctors(DoctorsDecision.Heal(others.shuffled(random).take(healCount(game.players, actors)).map { it.id }))
                    }
                }
                NightStep.ComputerScientist -> game.playComputerScientist()
                NightStep.Psychologist -> game.playPsychologist(someone(actors.single()))
                NightStep.Geneticist -> game.playGeneticist(someone(actors.single()))
                NightStep.Hacker -> game.playHacker(hackableSteps(game.variants).random(random))
                NightStep.Spy -> game.playSpy(someone(actors.single()))
                NightStep.Informant -> game.playInformant(someone(actors.single()))
                NightStep.Investigator -> game.playInvestigator(someone(actors.single()))
            }
        }

        val day = game.day!!
        return when {
            game.captainToReplace != null -> game.nameNewCaptain(someone(game.captainToReplace))
            day.result is VoteResult.CaptainMustChoose -> game.closeVote((day.result as VoteResult.CaptainMustChoose).options.random(random))
            day.result != null -> game.startNight()
            game.variants.captain && game.captain == null && game.round == 1 && day.vote.ballots.isEmpty() ->
                game.electCaptain(alive.associate { it.id to someone() }, random)
                    .castVote(someone(), VoteOption.For(someone()))
            else -> {
                // Everyone votes, mostly for one of two suspects so votes are decisive.
                val suspects = listOf(someone(), someone())
                val voted = alive.fold(game) { g, voter ->
                    val option = when {
                        g.variants.blankVoteAllowed && random.nextInt(8) == 0 -> VoteOption.Blank
                        else -> VoteOption.For(if (random.nextInt(5) == 0) someone() else suspects.random(random))
                    }
                    g.castVote(voter.id, option)
                }
                voted.closeVote()
            }
        }
    }

    private fun check(seed: Int, before: GameState, after: GameState, dealt: Map<PlayerId, Pair<Role, Genome>>) {
        val where = "seed $seed, round ${after.round}"
        assertEquals(before.players.size, after.players.size, "$where: player count")
        after.players.forEach { player ->
            assertEquals(dealt.getValue(player.id), player.role to player.genome, "$where: role and genome never change")
            val previous = before.players.single { it.id == player.id }
            assertTrue(previous.alive || !player.alive, "$where: ${player.id} came back to life")
            if (player.role == Role.BaseMutant) assertEquals(Genome.Host, player.genome, "$where: base mutant is a host")
            if (player.isMutated && player.genome == Genome.Resistant) throw AssertionError("$where: a resistant player was mutated")
            if (previous.isMutated && !player.isMutated) assertTrue(player.genome != Genome.Host, "$where: a host was healed")
            if (previous.isMutated && !player.isMutated) assertTrue(player.role != Role.Traitor, "$where: the traitor was healed")
        }
        assertTrue(after.journal.size >= before.journal.size && after.journal.take(before.journal.size) == before.journal, "$where: journal only grows")

        // The paralysed player is never woken for the rest of the night (R-NIGHT-22).
        after.night?.progress?.let { progress ->
            val call = progress.current
            if (progress.paralysed != null && call != null && call.step != NightStep.Mutants) {
                assertTrue(progress.paralysed !in call.actors, "$where: ${progress.paralysed} played while paralysed")
            }
        }

        // Victory is checked at dawn and after the vote (R-NIGHT-20, R-DAY-12), not mid-night.
        val victory = checkVictory(after.players)
        if (after.phase == Phase.Ended) assertTrue(after.end != null, "$where: ended without reason")
        if (after.phase == Phase.Day && victory != null) throw AssertionError("$where: $victory not applied")
        after.captain?.let { captain -> assertTrue(after.players.single { it.id == captain }.alive, "$where: dead captain") }
    }
}
