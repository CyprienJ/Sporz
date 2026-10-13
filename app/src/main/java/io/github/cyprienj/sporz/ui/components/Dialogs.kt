package io.github.cyprienj.sporz.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

/** Asks before an action that loses something ("Arrêter et créer", "Supprimer"). */
@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SporzTheme.colors.background,
        title = { Text(title.uppercase(), style = SporzTypography.heading) },
        text = { Text(body, style = SporzTypography.body, color = SporzTheme.colors.textBody) },
        confirmButton = { SporzButton(confirmLabel, onConfirm, height = 44.dp) },
        dismissButton = {
            SporzButton(stringResource(R.string.action_cancel), onDismiss, style = SporzButtonStyle.Secondary, height = 44.dp)
        },
    )
}
