package io.github.cyprienj.sporz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

enum class SporzButtonStyle {
    /** Orange call to action ("Ouvrir le lobby", "Valider : soigner Chloé"). */
    Primary,

    /** Outlined ("Suggérer", "Tuer", "Masquer l'écran"). */
    Secondary,

    /** Filled with the text color: navy on light, off-white on dark ("Soigner" when chosen). */
    Inverse,
}

@Composable
fun SporzButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SporzButtonStyle = SporzButtonStyle.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
) {
    val colors = SporzTheme.colors
    val (container, content) = when {
        !enabled && style != SporzButtonStyle.Secondary -> colors.disabled to colors.onDisabled
        !enabled -> Color.Transparent to colors.onDisabled
        style == SporzButtonStyle.Primary -> colors.accent to colors.onAccent
        style == SporzButtonStyle.Secondary -> Color.Transparent to colors.text
        else -> colors.inverse to colors.onInverse
    }
    val border = if (style == SporzButtonStyle.Secondary) {
        BorderStroke(1.dp, if (enabled) colors.outline else colors.disabled)
    } else null
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = SporzShapes.medium,
        color = container,
        contentColor = content,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = text,
                style = if (style == SporzButtonStyle.Secondary) {
                    SporzTypography.button.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                } else {
                    SporzTypography.button
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Square icon button of the design (back arrow, remove player, add player). */
@Composable
fun SporzIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = if (filled) SporzTheme.colors.onInverse else SporzTheme.colors.text,
    size: Dp = 48.dp,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = SporzShapes.medium,
        color = if (filled) SporzTheme.colors.inverse else Color.Transparent,
        contentColor = tint,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.padding((size - 22.dp) / 2))
    }
}

/** Big choice card of the home screen ("Créer une partie", "Rejoindre une partie"). */
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val colors = SporzTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = SporzShapes.large,
        color = if (primary) colors.accent else colors.card,
        contentColor = if (primary) colors.onAccent else colors.text,
        border = if (primary) null else BorderStroke(1.dp, colors.cardBorder),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), style = SporzTypography.heading)
                Text(
                    subtitle,
                    style = SporzTypography.label.copy(fontWeight = FontWeight.Normal),
                    color = if (primary) colors.onAccent.copy(alpha = 0.85f) else colors.textMuted,
                )
            }
            Icon(SporzIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
