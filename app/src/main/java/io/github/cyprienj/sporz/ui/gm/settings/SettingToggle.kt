package io.github.cyprienj.sporz.ui.gm.settings

import androidx.annotation.StringRes
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.GameSettings

/** The on/off settings of the design, with their texts and where they apply. */
enum class SettingToggle(
    @StringRes val label: Int,
    @StringRes val hint: Int,
    /** True when the setting is about the players' phones (not used offline). */
    val needsPhones: Boolean,
    val get: (GameSettings) -> Boolean,
    val set: (GameSettings, Boolean) -> GameSettings,
) {
    NightActionsOnPhone(R.string.setting_night_actions_on_phone, R.string.setting_night_actions_on_phone_hint, true, { it.nightActionsOnPhone }, { s, v -> s.copy(nightActionsOnPhone = v) }),
    GuidedCall(R.string.setting_guided_call, R.string.setting_guided_call_hint, false, { it.guidedCall }, { s, v -> s.copy(guidedCall = v) }),
    Vibrate(R.string.setting_vibrate, R.string.setting_vibrate_hint, true, { it.vibrate }, { s, v -> s.copy(vibrate = v) }),
    AutoReports(R.string.setting_auto_reports, R.string.setting_auto_reports_hint, true, { it.autoReports }, { s, v -> s.copy(autoReports = v) }),
    NotifyMutation(R.string.setting_notify_mutation, R.string.setting_notify_mutation_hint, true, { it.notifyMutation }, { s, v -> s.copy(notifyMutation = v) }),
    Notebook(R.string.setting_notebook, R.string.setting_notebook_hint, true, { it.notebook }, { s, v -> s.copy(notebook = v) }),
    TurnReminder(R.string.setting_turn_reminder, R.string.setting_turn_reminder_hint, true, { it.turnReminder }, { s, v -> s.copy(turnReminder = v) }),
    VoteOnPhone(R.string.setting_vote_on_phone, R.string.setting_vote_on_phone_hint, true, { it.voteOnPhone }, { s, v -> s.copy(voteOnPhone = v) }),
    AutoCloseVote(R.string.setting_auto_close_vote, R.string.setting_auto_close_vote_hint, true, { it.autoCloseVote }, { s, v -> s.copy(autoCloseVote = v) }),
    LiveTally(R.string.setting_live_tally, R.string.setting_live_tally_hint, true, { it.liveTally }, { s, v -> s.copy(liveTally = v) }),
    AutoHideRole(R.string.setting_auto_hide_role, R.string.setting_auto_hide_role_hint, true, { it.autoHideRole }, { s, v -> s.copy(autoHideRole = v) }),
    ;

    companion object {
        /** Sections of the design, in order. */
        val sections: List<Pair<Int, List<SettingToggle>>> = listOf(
            R.string.settings_section_night to listOf(NightActionsOnPhone, GuidedCall, Vibrate),
            R.string.settings_section_information to listOf(AutoReports, NotifyMutation, Notebook, TurnReminder),
            R.string.settings_section_votes to listOf(VoteOnPhone, AutoCloseVote, LiveTally),
            R.string.settings_section_privacy to listOf(AutoHideRole),
        )
    }
}
