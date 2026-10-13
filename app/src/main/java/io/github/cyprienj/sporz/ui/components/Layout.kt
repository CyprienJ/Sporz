package io.github.cyprienj.sporz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SurfaceKind
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

/**
 * Top of a screen: optional back arrow and overline ("Étape 3 sur 3"), then
 * the uppercase title and an optional trailing slot ("10 / 10 joueurs").
 * Uses the current surface colors, so it also works inside a SecretSurface.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: (() -> Unit)? = null,
    titleStyle: TextStyle = SporzTypography.title,
    divider: Boolean = SporzTheme.colors.kind == SurfaceKind.Standard,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = SporzTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, top = if (onBack != null) 8.dp else 18.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onBack != null || overline != null) {
                Row(
                    Modifier.offset(x = if (onBack != null) (-12).dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onBack != null) {
                        SporzIconButton(SporzIcons.Back, stringResource(R.string.action_back), onBack)
                    }
                    if (overline != null) {
                        Text(overline, style = SporzTypography.label.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                    }
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title.uppercase(), style = titleStyle, modifier = Modifier.weight(1f))
                trailing()
            }
        }
        if (divider) HorizontalDivider(color = colors.cardBorder)
    }
}

/** Uppercase grey section title ("GÉNOMES", "VARIANTES", "RAPPORTS REÇUS"). */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = SporzTypography.section, color = SporzTheme.colors.textMuted, modifier = modifier)
}

/** Small spaced uppercase label ("TON RÔLE", "RÉSULTAT"): orange in a secret area, grey elsewhere. */
@Composable
fun Overline(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val colors = SporzTheme.colors
    Text(
        text.uppercase(),
        style = SporzTypography.overline,
        color = color.takeOrElse { if (colors.kind == SurfaceKind.Secret) colors.accent else colors.textMuted },
        modifier = modifier,
    )
}

/**
 * White card with a thin border; orange 2 dp border when [highlighted] (current
 * night step). In a secret area, a highlighted card is also tinted (chosen night target).
 */
@Composable
fun SporzCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SporzTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(if (highlighted && colors.kind == SurfaceKind.Secret) colors.selected else colors.card, SporzShapes.medium)
            .border(
                if (highlighted) BorderStroke(2.dp, colors.selectedBorder) else BorderStroke(1.dp, colors.cardBorder),
                SporzShapes.medium,
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** Rounded group of rows separated by [GroupDivider] (settings, crew list). */
@Composable
fun CardGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = SporzTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SporzShapes.large,
        color = colors.card,
        contentColor = colors.text,
        border = BorderStroke(1.dp, colors.cardBorder),
    ) {
        Column(content = content)
    }
}

@Composable
fun GroupDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = SporzTheme.colors.divider)
}

/** Grey information box with an info icon ("Le MJ ne joue pas…"). */
@Composable
fun InfoNote(text: String, modifier: Modifier = Modifier, icon: ImageVector = SporzIcons.Info) {
    val colors = SporzTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .then(
                if (colors.kind == SurfaceKind.Secret) {
                    Modifier.border(1.dp, colors.cardBorder, SporzShapes.medium)
                } else {
                    Modifier.background(colors.neutral.background, SporzShapes.medium)
                },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val color = colors.textBody
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(text, style = SporzTypography.label.copy(fontWeight = FontWeight.Normal, lineHeight = 20.sp), color = color)
    }
}

/** Number + label tile (genome counts, crew stats in the board header). */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SporzTheme.colors.text,
) {
    val colors = SporzTheme.colors
    val secret = colors.kind == SurfaceKind.Secret
    Column(
        modifier
            .then(
                if (secret) {
                    Modifier.background(colors.card, SporzShapes.small)
                } else {
                    Modifier
                        .background(colors.card, SporzShapes.medium)
                        .border(1.dp, colors.cardBorder, SporzShapes.medium)
                },
            )
            .padding(horizontal = if (secret) 10.dp else 14.dp, vertical = if (secret) 8.dp else 12.dp),
    ) {
        // Standard tiles show the label first (genomes), secret ones the number first (crew stats).
        if (secret) {
            Text(value, style = SporzTypography.heading, color = valueColor)
            Text(label, style = SporzTypography.caption, color = colors.textMuted)
        } else {
            Text(label, style = SporzTypography.small, color = colors.textMuted)
            Text(value, style = SporzTypography.numberLarge, color = valueColor)
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)

/** Bottom navigation of the running game: Phase / Joueurs / Journal (MJ), Rôle / Joueurs / Journal (player). */
@Composable
fun SporzTabBar(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SporzTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.card),
    ) {
        HorizontalDivider(color = colors.cardBorder)
        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Column(
                    Modifier
                        .weight(1f)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        Modifier
                            .background(if (selected) colors.accentSoft else Color.Transparent, SporzShapes.pill)
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = if (selected) colors.text else colors.textMuted,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        item.label,
                        color = if (selected) colors.text else colors.textMuted,
                        style = SporzTypography.caption.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                    )
                }
            }
        }
    }
}

/** Labelled text input ("Ton prénom", "Ajouter un joueur hors ligne"). */
@Composable
fun SporzTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
) {
    val colors = SporzTheme.colors
    var focused by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) Text(label, style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            textStyle = SporzTypography.bodyLarge.copy(fontSize = 17.sp, color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Box(
                    Modifier
                        .background(colors.card, SporzShapes.medium)
                        .border(
                            if (focused) BorderStroke(2.dp, colors.text) else BorderStroke(1.dp, colors.outline),
                            SporzShapes.medium,
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = SporzTypography.bodyLarge, color = colors.iconMuted)
                    }
                    inner()
                }
            },
        )
        if (supportingText != null) Text(supportingText, style = SporzTypography.small, color = colors.textMuted)
    }
}

/** Big centered input for the 4-character game code ("KX7Q"). Keeps letters and digits, uppercased. */
@Composable
fun GameCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    length: Int = 4,
) {
    val colors = SporzTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) Text(label, style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold))
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter { it.isLetterOrDigit() }.uppercase().take(length)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            textStyle = SporzTypography.titleLarge.copy(
                color = colors.text,
                letterSpacing = 0.4.em,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .background(colors.card, SporzShapes.large)
                        .border(2.dp, colors.text, SporzShapes.large)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) { inner() }
            },
        )
    }
}
