package io.github.cyprienj.sporz.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.components.ConfirmDialog

/**
 * Number of screens asking to keep the screen on. During a navigation (night →
 * day) both screens are composed for a moment, so the one leaving must not
 * switch it off for the one arriving.
 */
private var screenOnRequests = 0

/** Keeps the screen on while this is shown: the MJ reads the night calls without touching the phone. */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        screenOnRequests++
        view.keepScreenOn = true
        onDispose {
            screenOnRequests--
            if (screenOnRequests == 0) view.keepScreenOn = false
        }
    }
}

/**
 * A screen of the running game (ticket C14): screen kept on and, when
 * [onLeave] is set, Back asks before leaving the game (it stays resumable from home).
 */
@Composable
fun RunningGameScreen(onLeave: (() -> Unit)? = null, content: @Composable () -> Unit) {
    KeepScreenOn()
    content()
    if (onLeave == null) return
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    BackHandler { confirmLeave = true }
    if (confirmLeave) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_leave_title),
            body = stringResource(R.string.confirm_leave_body),
            confirmLabel = stringResource(R.string.action_leave),
            onConfirm = {
                confirmLeave = false
                onLeave()
            },
            onDismiss = { confirmLeave = false },
        )
    }
}
