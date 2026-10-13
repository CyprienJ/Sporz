package io.github.cyprienj.sporz.engine.day

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.day.VoteOption.Blank
import io.github.cyprienj.sporz.engine.day.VoteOption.For
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class VoteTest {

    /** Crew of design.html. */
    private val crew = listOf(
        p("Alice", Role.Hacker),
        p("Bastien", Role.Doctor),
        p("Chloé", Role.Astronaut),
        p("Dorian", Role.Psychologist),
        p("Emma", Role.Doctor),
        p("Farid", Role.Spy, Genome.Resistant),
        p("Gaëlle", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
        p("Hugo", Role.Astronaut),
        p("Inès", Role.ComputerScientist, Genome.Host),
        p("Jules", Role.Geneticist),
    )
    private val variants = Variants()

    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    private fun id(name: String) = PlayerId(name)
    private fun forP(name: String) = For(id(name))
    private fun List<Player>.update(name: String, change: (Player) -> Player) = map { if (it.name == name) change(it) else it }

    /** Builds a vote from voter → option ("" = blank). */
    private fun vote(players: List<Player>, vararg ballots: Pair<String, String>, v: Variants = variants) =
        ballots.fold(Vote(1)) { vote, (voter, target) -> vote.cast(players, id(voter), if (target.isEmpty()) Blank else forP(target), v) }

    @Test
    fun `E-5 relative majority ejects without threshold (R-DAY-9, DEC-22)`() {
        val players = crew.update("Hugo") { it.copy(alive = false) }
        // 9 living: Gaëlle 4, Chloé 1, blank 1, 3 abstentions.
        val v = vote(players, "Alice" to "Gaëlle", "Bastien" to "Gaëlle", "Dorian" to "Gaëlle", "Jules" to "Gaëlle", "Emma" to "Chloé", "Farid" to "")
        val closing = closeVote(players, v, variants)
        assertEquals(VoteResult.Ejected(id("Gaëlle"), 4, Role.BaseMutant, PlayerState.Mutated), closing.result)
        assertFalse(closing.players.single { it.name == "Gaëlle" }.alive)
    }

    @Test
    fun `design journal - Hugo ejected with 5 votes out of 10, 1 blank`() {
        val v = vote(
            crew,
            "Alice" to "Hugo", "Bastien" to "Hugo", "Chloé" to "Hugo", "Dorian" to "Hugo", "Emma" to "Hugo",
            "Farid" to "Gaëlle", "Gaëlle" to "Farid", "Hugo" to "Gaëlle", "Inès" to "", "Jules" to "Farid",
        )
        assertEquals(VoteResult.Ejected(id("Hugo"), 5, Role.Astronaut, PlayerState.Healthy), closeVote(crew, v, variants).result)
    }

    @Test
    fun `blank first means nobody is ejected (R-DAY-9)`() {
        val v = vote(crew, "Alice" to "", "Bastien" to "", "Chloé" to "Gaëlle")
        val closing = closeVote(crew, v, variants)
        assertEquals(VoteResult.BlankWins, closing.result)
        assertEquals(crew, closing.players)
    }

    @Test
    fun `a tie ejects nobody without captain (R-DAY-10)`() {
        val v = vote(crew, "Alice" to "Gaëlle", "Bastien" to "Chloé", "Dorian" to "")
        assertEquals(VoteResult.Tie(listOf(forP("Gaëlle"), forP("Chloé"), Blank)), closeVote(crew, v, variants).result)
    }

    @Test
    fun `no ballots, no ejection (R-DAY-8)`() {
        assertEquals(VoteResult.NoVotes, closeVote(crew, Vote(1), variants).result)
    }

    @Test
    fun `votes can be changed or withdrawn until the close (R-DAY-7, R-DAY-8)`() {
        var v = vote(crew, "Alice" to "Gaëlle")
        v = v.cast(crew, id("Alice"), forP("Chloé"), variants)
        assertEquals(mapOf(id("Alice") to forP("Chloé")), v.ballots)
        assertEquals(emptyMap<PlayerId, VoteOption>(), v.withdraw(id("Alice")).ballots)
    }

    @Test
    fun `players may vote for themselves (R-DAY-7)`() {
        assertEquals(mapOf(id("Alice") to forP("Alice")), vote(crew, "Alice" to "Alice").ballots)
    }

    @Test
    fun `dead players cannot vote nor be voted for (R-DAY-2, R-DAY-7)`() {
        val players = crew.update("Hugo") { it.copy(alive = false) }
        assertEquals(VoteProblem.VoterDead, assertThrows<IllegalVoteException> { vote(players, "Hugo" to "Alice") }.problem)
        assertEquals(VoteProblem.TargetDead, assertThrows<IllegalVoteException> { vote(players, "Alice" to "Hugo") }.problem)
    }

    @Test
    fun `blank vote needs the variant (R-DAY-7)`() {
        val noBlank = Variants(blankVoteAllowed = false)
        assertEquals(VoteProblem.BlankNotAllowed, assertThrows<IllegalVoteException> { vote(crew, "Alice" to "", v = noBlank) }.problem)
    }

    @Test
    fun `without the reveal variant the role and state stay hidden (R-DAY-11)`() {
        val noReveal = Variants(revealOnElimination = false)
        val v = vote(crew, "Alice" to "Gaëlle", v = noReveal)
        assertEquals(VoteResult.Ejected(id("Gaëlle"), 1), closeVote(crew, v, noReveal).result)
    }

    // --- Captain ---------------------------------------------------------------

    private val withCaptain = Variants(captain = true)

    @Test
    fun `the captain's vote counts double (R-DAY-5)`() {
        // Gaëlle 2 votes vs Chloé 1 + captain Emma (2) = 3.
        val v = vote(crew, "Alice" to "Gaëlle", "Bastien" to "Gaëlle", "Dorian" to "Chloé", "Emma" to "Chloé", v = withCaptain)
        assertEquals(VoteResult.Ejected(id("Chloé"), 3, Role.Astronaut, PlayerState.Healthy), closeVote(crew, v, withCaptain, captain = id("Emma")).result)
        // Without the variant, the same ballots tie.
        assertTrue(closeVote(crew, v, variants, captain = id("Emma")).result is VoteResult.Tie)
    }

    @Test
    fun `the captain breaks ties (R-DAY-10)`() {
        val v = vote(crew, "Alice" to "Gaëlle", "Bastien" to "Chloé", v = withCaptain)
        val first = closeVote(crew, v, withCaptain, captain = id("Emma"))
        assertEquals(VoteResult.CaptainMustChoose(listOf(forP("Gaëlle"), forP("Chloé"))), first.result)
        assertEquals(crew, first.players)

        val second = closeVote(crew, v, withCaptain, captain = id("Emma"), captainChoice = forP("Gaëlle"))
        assertTrue(second.result is VoteResult.Ejected && (second.result as VoteResult.Ejected).player == id("Gaëlle"))

        assertEquals(
            VoteProblem.NotATiedOption,
            assertThrows<IllegalVoteException> { closeVote(crew, v, withCaptain, id("Emma"), forP("Hugo")) }.problem,
        )
    }

    @Test
    fun `a dead captain cannot break a tie`() {
        val players = crew.update("Emma") { it.copy(alive = false) }
        val v = vote(players, "Alice" to "Gaëlle", "Bastien" to "Chloé", v = withCaptain)
        assertTrue(closeVote(players, v, withCaptain, captain = id("Emma")).result is VoteResult.Tie)
    }

    @Test
    fun `captain election - most votes wins, ties drawn at random (R-DAY-4)`() {
        val clear = electCaptain(crew, mapOf(id("Alice") to id("Emma"), id("Bastien") to id("Emma"), id("Chloé") to id("Jules")), Random(0))
        assertEquals(CaptainElection(id("Emma"), 2, drawn = false), clear)

        val tied = mapOf(id("Alice") to id("Emma"), id("Bastien") to id("Jules"))
        val winners = (0 until 200).map { electCaptain(crew, tied, Random(it))!!.captain }.toSet()
        assertEquals(setOf(id("Emma"), id("Jules")), winners)
        assertTrue(electCaptain(crew, tied, Random(1))!!.drawn)
        assertEquals(electCaptain(crew, tied, Random(7)), electCaptain(crew, tied, Random(7)))

        assertEquals(null, electCaptain(crew, emptyMap(), Random(0)))
    }

    @Test
    fun `a dying captain names another living player (R-DAY-6)`() {
        val players = crew.update("Hugo") { it.copy(alive = false) }
        assertEquals(id("Jules"), passCaptaincy(players, id("Emma"), id("Jules")))
        assertThrows<IllegalVoteException> { passCaptaincy(players, id("Emma"), id("Emma")) }
        assertThrows<IllegalVoteException> { passCaptaincy(players, id("Emma"), id("Hugo")) }
    }

    @Test
    fun `votes and results round-trip through JSON`() {
        val v = vote(crew, "Alice" to "Gaëlle", "Bastien" to "")
        assertEquals(v, EngineJson.decodeFromString<Vote>(EngineJson.encodeToString(v)))
        val results: List<VoteResult> = listOf(
            VoteResult.Ejected(id("Hugo"), 5, Role.Astronaut, PlayerState.Healthy),
            VoteResult.BlankWins, VoteResult.NoVotes, VoteResult.Tie(listOf(Blank, forP("Chloé"))),
            VoteResult.CaptainMustChoose(listOf(forP("Alice"))),
        )
        assertEquals(results, EngineJson.decodeFromString<List<VoteResult>>(EngineJson.encodeToString(results)))
    }
}
