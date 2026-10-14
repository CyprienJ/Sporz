package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.setup.Seat
import io.github.cyprienj.sporz.engine.setup.SeatProblem
import io.github.cyprienj.sporz.ui.components.Avatar
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.PlayerRow
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzIconButton
import io.github.cyprienj.sporz.ui.components.SporzTextField
import io.github.cyprienj.sporz.ui.components.TextLink
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

/** design.html, board "MJ · Lobby": the MJ types the players' names. */
@Composable
fun LobbyRoute(
    onBack: () -> Unit,
    onChooseRoles: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LobbyViewModel = hiltViewModel(),
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    LobbyScreen(
        seats = draft?.seats.orEmpty(),
        newName = viewModel.newName,
        addProblem = viewModel.addProblem,
        onNewNameChange = viewModel::onNewNameChange,
        onAdd = viewModel::add,
        onRemove = viewModel::remove,
        onRename = viewModel::startRenaming,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onChooseRoles = onChooseRoles,
    )
    viewModel.renaming?.let { renaming ->
        RenameDialog(
            name = renaming.name,
            problem = renaming.problem,
            onNameChange = viewModel::onRenameChange,
            onConfirm = viewModel::confirmRename,
            onDismiss = viewModel::cancelRename,
        )
    }
}

@Composable
fun LobbyScreen(
    seats: List<Seat>,
    newName: String,
    addProblem: SeatProblem?,
    onNewNameChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (PlayerId) -> Unit,
    onRename: (Seat) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onChooseRoles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val missing = LobbyViewModel.missingPlayers(seats)
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) {
            ScreenHeader(
                title = stringResource(R.string.screen_lobby),
                overline = stringResource(R.string.lobby_step),
                onBack = onBack,
                titleStyle = SporzTypography.titleMedium,
            ) {
                TextLink(stringResource(R.string.screen_settings), onOpenSettings)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SporzTextField(
                        value = newName,
                        onValueChange = onNewNameChange,
                        placeholder = stringResource(R.string.lobby_add_placeholder),
                        supportingText = addProblem?.let { stringResource(it.messageRes) },
                        isError = addProblem != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onAdd() }),
                        modifier = Modifier.weight(1f),
                    )
                    SporzIconButton(
                        icon = SporzIcons.Plus,
                        contentDescription = stringResource(R.string.action_add),
                        onClick = onAdd,
                        filled = true,
                    )
                }
            }
            item {
                // Wraps the hint under the title with large fonts.
                FlowRow(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    itemVerticalAlignment = Alignment.Bottom,
                ) {
                    SectionTitle(stringResource(R.string.lobby_crew, seats.size), Modifier.padding(end = 12.dp))
                    if (seats.isNotEmpty()) {
                        Text(stringResource(R.string.lobby_rename_hint), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
                    }
                }
            }
            if (seats.isEmpty()) {
                item { Text(stringResource(R.string.lobby_empty), style = SporzTypography.body, color = SporzTheme.colors.textMuted) }
            } else {
                item {
                    CardGroup {
                        seats.forEachIndexed { index, seat ->
                            if (index > 0) GroupDivider()
                            PlayerRow(
                                name = seat.name,
                                onClick = { onRename(seat) },
                                leading = { Avatar(seat.name) },
                                trailing = {
                                    SporzIconButton(
                                        icon = SporzIcons.Close,
                                        contentDescription = stringResource(R.string.lobby_remove, seat.name),
                                        onClick = { onRemove(seat.id) },
                                        tint = SporzTheme.colors.iconMuted,
                                        size = 40.dp,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (missing > 0) {
                Text(
                    pluralStringResource(R.plurals.lobby_missing_players, missing, missing),
                    style = SporzTypography.small,
                    color = SporzTheme.colors.textMuted,
                )
            }
            SporzButton(
                text = stringResource(R.string.action_choose_roles),
                onClick = onChooseRoles,
                enabled = missing == 0,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RenameDialog(
    name: String,
    problem: SeatProblem?,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SporzTheme.colors.background,
        title = { Text(stringResource(R.string.lobby_rename_title).uppercase(), style = SporzTypography.heading) },
        text = {
            SporzTextField(
                value = name,
                onValueChange = onNameChange,
                label = stringResource(R.string.lobby_add_label),
                supportingText = problem?.let { stringResource(it.messageRes) },
                isError = problem != null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            )
        },
        confirmButton = { SporzButton(stringResource(R.string.action_save), onConfirm, height = 44.dp) },
        dismissButton = { SporzButton(stringResource(R.string.action_cancel), onDismiss, style = SporzButtonStyle.Secondary, height = 44.dp) },
    )
}

private val SeatProblem.messageRes: Int
    get() = when (this) {
        SeatProblem.BlankName -> R.string.lobby_error_blank
        SeatProblem.NameTooLong -> R.string.lobby_error_too_long
        SeatProblem.NameTaken -> R.string.lobby_error_taken
        SeatProblem.LobbyFull -> R.string.lobby_error_full
    }

private val previewSeats = listOf("Bastien", "Gaëlle", "Hugo").map { Seat(PlayerId(it), it) }

@Preview(name = "Light")
@Composable
private fun LobbyLightPreview() {
    SporzTheme(darkTheme = false) {
        LobbyScreen(previewSeats, "Ali", null, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Dark, name taken")
@Composable
private fun LobbyDarkPreview() {
    SporzTheme(darkTheme = true) {
        LobbyScreen(previewSeats, "hugo", SeatProblem.NameTaken, {}, {}, {}, {}, {}, {}, {})
    }
}
