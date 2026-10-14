package io.github.cyprienj.sporz.ui.gm.setup

import io.github.cyprienj.sporz.data.GameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DistributionViewModelTest {

    private val session = GameSession().apply {
        startSetup()
        repeat(7) { addSeat("P$it") }
        CompositionViewModel(this).deal(Random(3))
    }
    private val viewModel = DistributionViewModel(session)
    private val players = session.game.value!!.players

    @Test
    fun `only one role is shown at a time`() {
        viewModel.reveal(players[0].id)
        viewModel.reveal(players[1].id)
        assertEquals(players[1].id, viewModel.revealed)
        viewModel.hide()
        assertNull(viewModel.revealed)
    }

    @Test
    fun `confirming marks the player announced and hides the role`() {
        viewModel.reveal(players[0].id)
        viewModel.confirmAnnounced(players[0].id)
        assertEquals(setOf(players[0].id), session.announced.value)
        assertNull(viewModel.revealed)
    }

    @Test
    fun `an announced role can be shown again and stays announced`() {
        viewModel.confirmAnnounced(players[0].id)
        viewModel.reveal(players[0].id)
        assertEquals(players[0].id, viewModel.revealed)
        viewModel.hide()
        assertEquals(setOf(players[0].id), session.announced.value)
    }

    @Test
    fun `the night starts once every role is announced`() {
        players.dropLast(1).forEach { viewModel.confirmAnnounced(it.id) }
        assertFalse(viewModel.canStartNight())
        viewModel.confirmAnnounced(players.last().id)
        assertTrue(viewModel.canStartNight())
    }

    @Test
    fun `dealing again resets the announcements`() {
        players.forEach { viewModel.confirmAnnounced(it.id) }
        CompositionViewModel(session).deal(Random(4))
        assertEquals(emptySet<Any>(), session.announced.value)
        assertFalse(DistributionViewModel(session).canStartNight())
    }
}
