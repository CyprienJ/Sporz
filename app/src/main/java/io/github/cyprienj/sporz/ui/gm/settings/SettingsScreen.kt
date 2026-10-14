package io.github.cyprienj.sporz.ui.gm.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzIconButton
import io.github.cyprienj.sporz.ui.components.Stepper
import io.github.cyprienj.sporz.ui.components.ToggleRow
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

/** design.html, board "MJ · Paramètres". */
@Composable
fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onToggle = viewModel::toggle,
        onDebateTimer = viewModel::setDebateTimer,
        onNightOrder = viewModel::setNightOrder,
        onBack = onBack,
    )
}

@Composable
fun SettingsScreen(
    settings: GameSettings,
    onToggle: (SettingToggle, Boolean) -> Unit,
    onDebateTimer: (Int) -> Unit,
    onNightOrder: (List<NightStep>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    var editingOrder by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) {
            Header(onBack = onBack)
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingToggle.sections.forEach { (title, toggles) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle(stringResource(title))
                    CardGroup {
                        toggles.forEachIndexed { index, toggle ->
                            if (index > 0) GroupDivider()
                            ToggleRow(
                                label = stringResource(toggle.label),
                                hint = stringResource(toggle.hint),
                                checked = toggle.get(settings),
                                onCheckedChange = { onToggle(toggle, it) },
                            )
                        }
                    }
                }
            }
            Flow(settings, onDebateTimer, onEditOrder = { editingOrder = true })
        }
    }
    if (editingOrder) {
        NightOrderDialog(
            initial = settings.callOrder,
            onSave = {
                onNightOrder(it)
                editingOrder = false
            },
            onDismiss = { editingOrder = false },
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Column(
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.offset(x = (-12).dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SporzIconButton(SporzIcons.Back, stringResource(R.string.action_back), onBack)
            Text(
                stringResource(R.string.screen_settings).uppercase(),
                style = SporzTypography.titleSmall,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}

@Composable
private fun Flow(settings: GameSettings, onDebateTimer: (Int) -> Unit, onEditOrder: () -> Unit) {
    val minutes = settings.debateTimerMinutes
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(R.string.settings_section_flow))
        CardGroup {
            Row(Modifier.padding(start = 14.dp, top = 10.dp, end = 6.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.setting_debate_timer), style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold))
                    Text(stringResource(R.string.setting_debate_timer_hint), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
                }
                Stepper(
                    value = if (minutes == 0) stringResource(R.string.setting_timer_off) else stringResource(R.string.setting_timer_minutes, minutes),
                    onDecrement = { onDebateTimer(minutes - 1) },
                    onIncrement = { onDebateTimer(minutes + 1) },
                    decrementLabel = stringResource(R.string.setting_timer_less),
                    incrementLabel = stringResource(R.string.setting_timer_more),
                    canDecrement = minutes > 0,
                    canIncrement = minutes < GameSettings.MAX_DEBATE_MINUTES,
                    valueWidth = 64.dp,
                )
            }
            GroupDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onEditOrder)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.setting_night_order), style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        settings.callOrder.map { stringResource(it.nameRes) }.joinToString(", "),
                        style = SporzTypography.small,
                        color = SporzTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(SporzIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun NightOrderDialog(initial: List<NightStep>, onSave: (List<NightStep>) -> Unit, onDismiss: () -> Unit) {
    var order by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SporzTheme.colors.background,
        title = { Text(stringResource(R.string.night_order_title).uppercase(), style = SporzTypography.heading) },
        text = {
            CardGroup {
                order.forEachIndexed { index, step ->
                    if (index > 0) GroupDivider()
                    val name = stringResource(step.nameRes)
                    Row(Modifier.padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", style = SporzTypography.number, modifier = Modifier.width(28.dp), color = SporzTheme.colors.accent)
                        Text(name, style = SporzTypography.bodyStrong, modifier = Modifier.weight(1f))
                        SporzIconButton(SporzIcons.ChevronUp, stringResource(R.string.night_order_up, name), { order = SettingsViewModel.move(order, index, -1) }, size = 44.dp)
                        SporzIconButton(SporzIcons.ChevronDown, stringResource(R.string.night_order_down, name), { order = SettingsViewModel.move(order, index, 1) }, size = 44.dp)
                    }
                }
            }
        },
        confirmButton = { SporzButton(stringResource(R.string.action_save), { onSave(order) }, height = 44.dp) },
        dismissButton = { SporzButton(stringResource(R.string.action_cancel), onDismiss, style = SporzButtonStyle.Secondary, height = 44.dp) },
    )
}

@Preview(name = "Light")
@Composable
private fun SettingsLightPreview() {
    SporzTheme(darkTheme = false) { SettingsScreen(GameSettings(), { _, _ -> }, {}, {}, {}) }
}

@Preview(name = "Dark")
@Composable
private fun SettingsDarkPreview() {
    SporzTheme(darkTheme = true) { SettingsScreen(GameSettings(), { _, _ -> }, {}, {}, {}) }
}
