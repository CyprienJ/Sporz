package io.github.cyprienj.sporz.ui.gm.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.NightStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Settings (ticket C6). Every change is saved in the session right away. */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    private val _settings = MutableStateFlow(session.currentSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun toggle(toggle: SettingToggle, on: Boolean) = update { toggle.set(it, on) }

    fun setDebateTimer(minutes: Int) =
        update { it.copy(debateTimerMinutes = minutes.coerceIn(0, GameSettings.MAX_DEBATE_MINUTES)) }

    fun setNightOrder(order: List<NightStep>) = update { it.copy(nightOrder = order) }

    private fun update(change: (GameSettings) -> GameSettings) {
        val updated = change(_settings.value)
        _settings.value = updated
        session.updateSettings(updated)
    }

    companion object {
        /** [order] with the step at [index] moved one place up (-1) or down (+1); unchanged at the ends. */
        fun move(order: List<NightStep>, index: Int, by: Int): List<NightStep> {
            val target = index + by
            if (index !in order.indices || target !in order.indices) return order
            return order.toMutableList().apply { add(target, removeAt(index)) }
        }
    }
}
