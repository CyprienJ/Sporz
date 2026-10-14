package io.github.cyprienj.sporz.ui.gm.settings

import androidx.annotation.StringRes
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.GameSettings

/** The on/off settings, with their texts. */
enum class SettingToggle(
    @StringRes val label: Int,
    @StringRes val hint: Int,
    val get: (GameSettings) -> Boolean,
    val set: (GameSettings, Boolean) -> GameSettings,
) {
    GuidedCall(R.string.setting_guided_call, R.string.setting_guided_call_hint, { it.guidedCall }, { s, v -> s.copy(guidedCall = v) }),
    ;

    companion object {
        /** Sections of the screen, in order. */
        val sections: List<Pair<Int, List<SettingToggle>>> = listOf(
            R.string.settings_section_night to listOf(GuidedCall),
        )
    }
}
