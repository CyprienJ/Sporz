package io.github.cyprienj.sporz.ui.gm.setup

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.setup.SeatProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LobbyViewModelTest {

    private val session = GameSession().apply { chooseMode(GameMode.Offline) }
    private val viewModel = LobbyViewModel(session)
    private fun names() = session.draft.value!!.seats.map { it.name }

    @Test
    fun `adding clears the field`() {
        viewModel.onNewNameChange("Alice")
        viewModel.add()
        assertEquals(listOf("Alice"), names())
        assertEquals("", viewModel.newName)
        assertNull(viewModel.addProblem)
    }

    @Test
    fun `a refused name keeps the text and shows why, until it changes`() {
        viewModel.onNewNameChange("Alice"); viewModel.add()
        viewModel.onNewNameChange("ALICE"); viewModel.add()
        assertEquals(SeatProblem.NameTaken, viewModel.addProblem)
        assertEquals("ALICE", viewModel.newName)
        viewModel.onNewNameChange("ALICE2")
        assertNull(viewModel.addProblem)
    }

    @Test
    fun `renaming flow`() {
        viewModel.onNewNameChange("Alice"); viewModel.add()
        viewModel.onNewNameChange("Bastien"); viewModel.add()
        val alice = session.draft.value!!.seats.first()

        viewModel.startRenaming(alice)
        viewModel.onRenameChange("bastien")
        viewModel.confirmRename()
        assertEquals(SeatProblem.NameTaken, viewModel.renaming!!.problem)

        viewModel.onRenameChange("Alicia")
        viewModel.confirmRename()
        assertNull(viewModel.renaming)
        assertEquals(listOf("Alicia", "Bastien"), names())
    }

    @Test
    fun `missing players before the roles can be chosen (R-SET-1)`() {
        assertEquals(7, LobbyViewModel.missingPlayers(emptyList()))
        repeat(7) { viewModel.onNewNameChange("P$it"); viewModel.add() }
        assertEquals(0, LobbyViewModel.missingPlayers(session.draft.value!!.seats))
    }

    @Test
    fun `opening the lobby without a setup starts an offline one`() {
        val fresh = GameSession()
        LobbyViewModel(fresh)
        assertEquals(GameMode.Offline, fresh.draft.value!!.mode)
    }
}
