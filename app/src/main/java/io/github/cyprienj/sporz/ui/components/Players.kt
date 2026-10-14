package io.github.cyprienj.sporz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SurfaceKind
import io.github.cyprienj.sporz.ui.theme.colors
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone

/** Round initial of a player. */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
) {
    val colors = SporzTheme.colors
    val (background, content) = when (colors.kind) {
        SurfaceKind.Secret -> colors.subtleFill to colors.text
        else -> colors.neutral.background to colors.neutral.content
    }
    Box(
        modifier
            .size(size)
            .background(background, SporzShapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1).uppercase(),
            color = content,
            style = SporzTypography.small.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

/**
 * Thin vertical bar showing a camp or a state at the start of a row. It fills
 * the row height, so the row needs a bounded height (`Modifier.height(IntrinsicSize.Min)`,
 * as in [PlayerRow]).
 */
@Composable
fun CampBar(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(color, RoundedCornerShape(2.dp)),
    )
}

/**
 * A player in a list (lobby, board, night targets on dark).
 * [leading] is usually an [Avatar] or a [CampBar]; [trailing] a badge or a button.
 */
@Composable
fun PlayerRow(
    name: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    eliminated: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .height(IntrinsicSize.Min)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (eliminated) 0.7f else 1f)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = SporzTypography.bodyStrong,
                    textDecoration = if (eliminated) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (subtitle != null) {
                Text(subtitle, style = SporzTypography.small, color = SporzTheme.colors.textMuted)
            }
        }
        trailing?.invoke(this)
    }
}

/** Colored pill: player state, genome, report result. */
@Composable
fun StatusBadge(text: String, tone: Tone, modifier: Modifier = Modifier) {
    val toneColors = tone.colors
    Text(
        text = text,
        color = toneColors.content,
        style = SporzTypography.small.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier
            .background(toneColors.background, SporzShapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Small uppercase label next to a name ("HORS LIGNE"). */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = SporzTheme.colors.neutral.content,
        style = SporzTypography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
        modifier = modifier
            .background(SporzTheme.colors.neutral.background, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

enum class StepState { Done, Current, Upcoming }

/** Numbered circle of a night step (MJ night screen). */
@Composable
fun StepIndicator(number: Int, state: StepState, modifier: Modifier = Modifier) {
    val colors = SporzTheme.colors
    val base = modifier.size(26.dp)
    when (state) {
        StepState.Done -> Box(base.background(colors.healthy.strong, SporzShapes.pill), contentAlignment = Alignment.Center) {
            Icon(SporzIcons.Check, contentDescription = null, tint = colors.card, modifier = Modifier.size(16.dp))
        }
        StepState.Current -> Box(base.background(colors.accent, SporzShapes.pill), contentAlignment = Alignment.Center) {
            Text("$number", color = colors.onAccent, style = SporzTypography.label.copy(fontWeight = FontWeight.Bold))
        }
        StepState.Upcoming -> Box(base.border(1.5.dp, colors.outline, SporzShapes.pill), contentAlignment = Alignment.Center) {
            Text("$number", color = colors.textMuted, style = SporzTypography.small.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
