package io.github.cyprienj.sporz.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.SurfaceKind

/** − value + counter (role counts, debate timer). */
@Composable
fun Stepper(
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementLabel: String,
    incrementLabel: String,
    modifier: Modifier = Modifier,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
    valueWidth: Dp = 44.dp,
) {
    val colors = SporzTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−", decrementLabel, onDecrement, canDecrement, colors.subtleFill, colors.text)
        Text(
            text = value,
            style = SporzTypography.number,
            textAlign = TextAlign.Center,
            // One line, wider with large fonts: a wrapped "5 min" would squeeze the buttons.
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = valueWidth).padding(horizontal = 4.dp),
        )
        StepperButton("+", incrementLabel, onIncrement, canIncrement, colors.inverse, colors.onInverse)
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    container: Color,
    content: Color,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .semantics { contentDescription = label },
        shape = SporzShapes.small,
        color = if (enabled) container else SporzTheme.colors.disabled,
        contentColor = if (enabled) content else SporzTheme.colors.onDisabled,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, style = SporzTypography.bodyStrong.copy(fontSize = 22.sp), modifier = Modifier.clearAndSetSemantics {})
        }
    }
}

/** On/off switch of the settings screen (46 × 28). */
@Composable
fun SporzSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = SporzTheme.colors
    val thumbOffset by animateDpAsState(if (checked) 18.dp else 0.dp, label = "thumb")
    Box(
        modifier
            .size(width = 46.dp, height = 28.dp)
            .clip(SporzShapes.pill)
            .background(if (checked) colors.accent else colors.outline)
            .padding(3.dp),
    ) {
        Box(
            Modifier
                .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                .size(22.dp)
                .clip(SporzShapes.pill)
                .background(if (checked) colors.onAccent else colors.switchThumbOff),
        )
    }
}

/** Setting row: label, optional hint, switch. The whole row is the touch target. */
@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold))
            if (hint != null) Text(hint, style = SporzTypography.small, color = SporzTheme.colors.textMuted)
        }
        SporzSwitch(checked)
    }
}

/**
 * Single choice card with a radio ring (game mode). A disabled card is dimmed
 * and cannot be chosen; [tag] explains why (e.g. "Bientôt"). Its texts stay
 * readable (muted colors, not transparency): the description is still useful.
 */
@Composable
fun RadioCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tag: String? = null,
) {
    val colors = SporzTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SporzShapes.large)
            .background(if (selected) colors.selected else colors.card)
            .border(
                if (selected) BorderStroke(2.dp, colors.selectedBorder) else BorderStroke(1.dp, colors.cardBorder),
                SporzShapes.large,
            )
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .alpha(if (enabled) 1f else 0.55f)
                .border(2.dp, if (selected) colors.selectedBorder else colors.iconMuted, SporzShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(10.dp).background(colors.selectedBorder, SporzShapes.pill))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title.uppercase(), style = SporzTypography.heading, color = if (enabled) colors.text else colors.textMuted)
                if (tag != null) Tag(tag)
            }
            Text(
                description,
                style = SporzTypography.body,
                color = if (enabled) colors.textBody else colors.textMuted,
            )
        }
    }
}

/**
 * Mutually exclusive options in a row. In a secret area (automation presets)
 * the options sit in a tinted track; elsewhere ("Soigner / Tuer") they are
 * separate outlined buttons.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SporzTheme.colors
    val track = colors.kind == SurfaceKind.Secret
    val container = if (track) {
        Modifier
            .background(colors.card, SporzShapes.medium)
            .padding(4.dp)
    } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(container)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(if (track) 6.dp else 8.dp),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val (background, content) = when {
                selected && track -> colors.accent to colors.onAccent
                selected -> colors.inverse to colors.onInverse
                track -> Color.Transparent to colors.text
                else -> colors.card to colors.text
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize() // 48 dp touch target (C14)
                    .height(44.dp)
                    .clip(SporzShapes.small)
                    .background(background)
                    .then(
                        if (!selected && !track) Modifier.border(1.dp, colors.outline, SporzShapes.small) else Modifier,
                    )
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    color = content,
                    style = SporzTypography.body.copy(
                        fontWeight = if (selected || track) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

/** Filter pill above a list (board filters). Selected = filled with the text color. */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SporzTheme.colors
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize() // 48 dp touch target (C14)
            .height(36.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        shape = SporzShapes.pill,
        color = if (selected) colors.inverse else colors.card,
        contentColor = if (selected) colors.onInverse else colors.text,
        border = BorderStroke(1.dp, if (selected) colors.inverse else colors.cardBorder),
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(label, style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

/**
 * Player pill used to pick a target (MJ night screen). A player who cannot be
 * targeted at this step is dimmed; a dead player ([eliminated]) is also struck
 * through, so the two are never confused.
 */
@Composable
fun TargetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    eliminated: Boolean = false,
    /** Read instead of [label] when two rows of chips show the same names. */
    contentDescription: String? = null,
) {
    val colors = SporzTheme.colors
    val unavailable = stringResource(R.string.state_unavailable)
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize() // 48 dp touch target (C14)
            .height(40.dp)
            .semantics {
                if (!enabled) stateDescription = unavailable
                if (contentDescription != null) this.contentDescription = contentDescription
            }
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = SporzShapes.pill,
        color = if (selected) colors.accentSoft else colors.card,
        contentColor = if (enabled) colors.text else colors.onDisabled,
        border = if (selected) BorderStroke(2.dp, colors.selectedBorder) else BorderStroke(1.dp, colors.cardBorder),
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = SporzTypography.label,
                textDecoration = if (eliminated) TextDecoration.LineThrough else null,
            )
        }
    }
}

/**
 * Select box of the design ("Votes hors ligne"): shows the chosen option or a
 * placeholder, and opens a menu of [options].
 */
@Composable
fun <T> SporzDropdown(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    placeholder: String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = SporzTheme.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            onClick = { open = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
            shape = SporzShapes.small,
            color = colors.card,
            contentColor = colors.text,
            border = BorderStroke(1.dp, colors.outline),
        ) {
            Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    selected?.let(label) ?: placeholder,
                    style = SporzTypography.body,
                    color = if (selected == null) colors.textMuted else colors.text,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Icon(SporzIcons.ChevronDown, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = colors.card) {
            DropdownMenuItem(text = { Text(placeholder, color = colors.textMuted) }, onClick = { open = false; onSelect(null) })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(label(option), color = colors.text) }, onClick = { open = false; onSelect(option) })
            }
        }
    }
}
