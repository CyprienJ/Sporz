package io.github.cyprienj.sporz.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Raw colors. The light ones come from design.html; the "Night*" ones are the
 * dark theme, derived from the design's navy. Components never use these
 * directly: they read [SporzColors] through `SporzTheme.colors`.
 */
object SporzPalette {
    // design.html
    val Navy = Color(0xFF12202B)
    val NavyCard = Color(0xFF1C2C39)
    val NavyPill = Color(0xFF22323F)
    val NavyLine = Color(0xFF2B3B48)
    val NavyLineStrong = Color(0xFF3A4A57)
    val NavySelected = Color(0xFF3A2A20)

    val Paper = Color(0xFFEEF0EB)
    val OffWhite = Color(0xFFF4F5F1)
    val White = Color(0xFFFFFFFF)

    val Line = Color(0xFFD6DAD3)
    val LineSoft = Color(0xFFECEEE9)
    val LineStrong = Color(0xFFB9BFB5)

    val Slate = Color(0xFF3C4A54)
    val Muted = Color(0xFF55626C)
    val Grey = Color(0xFF8A949B)
    val MutedOnNavy = Color(0xFFAEB8BF)
    val BodyOnNavy = Color(0xFFC9D1D6)

    val Orange = Color(0xFFC2410C)
    val OrangeLight = Color(0xFFFDBA74)
    val OrangeSoft = Color(0xFFFFE4D2)
    val OrangeTint = Color(0xFFFFF4EC)

    val Red = Color(0xFFB42318)
    val RedSoft = Color(0xFFFDE3E0)
    val RedOnNavy = Color(0xFFFCA5A5)

    val Teal = Color(0xFF0F766E)
    val TealDark = Color(0xFF0F5F58)
    val TealSoft = Color(0xFFDCEFEA)
    val TealOnNavy = Color(0xFF6EE7B7)

    val Blue = Color(0xFF1D4ED8)
    val BlueSoft = Color(0xFFE0ECFF)

    val Neutral = Color(0xFFE2E6DE)

    // Dark theme (not in design.html)
    val NightBackground = Color(0xFF0D151C)
    val NightCard = Color(0xFF16212B)
    val NightDivider = Color(0xFF1E2A35)
    val NightLine = Color(0xFF26333F)
    val NightFill = Color(0xFF22303C)
    val NightSelected = Color(0xFF2E2219)
    val NightMuted = Color(0xFF9AA5AD)
    val NightIcon = Color(0xFF6B7781)
    val SecretBackground = Color(0xFF070C11)
    val SecretCard = Color(0xFF111A22)
    val SecretLine = Color(0xFF2E3C48)

    val HealthyNight = Color(0xFF0F3B36)
    val HealthyStrongNight = Color(0xFF2DD4BF)
    val MutantNight = Color(0xFF4A1A16)
    val MutantStrongNight = Color(0xFFF87171)
    val NeutralNight = Color(0xFF26323C)
    val NeutralStrongNight = Color(0xFF4B5863)
    val ResistantNight = Color(0xFF1E2B5C)
    val ResistantOnNight = Color(0xFFA5C4FF)
    val ResistantStrongNight = Color(0xFF6B8EF5)
}

/** Semantic meaning of a badge, camp bar or stat (rules vocabulary). */
enum class Tone { Healthy, Mutant, Neutral, Resistant, Accent }

@Immutable
data class ToneColors(val background: Color, val content: Color, val strong: Color)

/**
 * The three kinds of area of the design. Components adapt their *layout* to
 * the kind (e.g. a stat tile shows its number first in a secret header) and
 * take every *color* from [SporzColors].
 */
enum class SurfaceKind {
    /** Normal screens: MJ setup, board, journal, settings list. */
    Standard,

    /** Areas hidden from other players or dramatized: role card, night action, dark headers. */
    Secret,

    /** A card that stands out inside a secret area (night action result). */
    Inset,
}

/** Colors of one surface in one theme. */
@Immutable
data class SporzColors(
    val kind: SurfaceKind,
    val background: Color,
    val text: Color,
    val textMuted: Color,
    val textBody: Color,
    val iconMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val card: Color,
    val cardBorder: Color,
    val divider: Color,
    val outline: Color,
    val subtleFill: Color,
    val inverse: Color,
    val onInverse: Color,
    val selected: Color,
    val selectedBorder: Color,
    val disabled: Color,
    val onDisabled: Color,
    val switchThumbOff: Color,
    val healthy: ToneColors,
    val mutant: ToneColors,
    val neutral: ToneColors,
    val resistant: ToneColors,
    val accentTone: ToneColors,
) {
    fun tone(tone: Tone) = when (tone) {
        Tone.Healthy -> healthy
        Tone.Mutant -> mutant
        Tone.Neutral -> neutral
        Tone.Resistant -> resistant
        Tone.Accent -> accentTone
    }
}

/** A theme = the colors of its three surfaces. */
@Immutable
data class SporzColorScheme(
    val isDark: Boolean,
    val standard: SporzColors,
    val secret: SporzColors,
    val inset: SporzColors,
)

private val LightTones = arrayOf(
    ToneColors(SporzPalette.TealSoft, SporzPalette.TealDark, SporzPalette.Teal),
    ToneColors(SporzPalette.RedSoft, SporzPalette.Red, SporzPalette.Red),
    ToneColors(SporzPalette.Neutral, SporzPalette.Slate, SporzPalette.LineStrong),
    ToneColors(SporzPalette.BlueSoft, SporzPalette.Blue, SporzPalette.Blue),
    ToneColors(SporzPalette.OrangeSoft, SporzPalette.Orange, SporzPalette.Orange),
)

private val DarkTones = arrayOf(
    ToneColors(SporzPalette.HealthyNight, SporzPalette.TealOnNavy, SporzPalette.HealthyStrongNight),
    ToneColors(SporzPalette.MutantNight, SporzPalette.RedOnNavy, SporzPalette.MutantStrongNight),
    ToneColors(SporzPalette.NeutralNight, SporzPalette.BodyOnNavy, SporzPalette.NeutralStrongNight),
    ToneColors(SporzPalette.ResistantNight, SporzPalette.ResistantOnNight, SporzPalette.ResistantStrongNight),
    ToneColors(SporzPalette.NavySelected, SporzPalette.OrangeLight, SporzPalette.OrangeLight),
)

private fun colors(
    kind: SurfaceKind,
    tones: Array<ToneColors>,
    background: Color,
    text: Color,
    textMuted: Color,
    textBody: Color,
    iconMuted: Color,
    /** Text on [disabled] fills: readable (4.5:1), the grey fill tells it is disabled. */
    onDisabled: Color = textMuted,
    accent: Color,
    onAccent: Color,
    accentSoft: Color,
    card: Color,
    cardBorder: Color,
    divider: Color,
    outline: Color,
    subtleFill: Color,
    onInverse: Color,
    selected: Color,
    disabled: Color,
    switchThumbOff: Color,
) = SporzColors(
    kind = kind,
    background = background,
    text = text,
    textMuted = textMuted,
    textBody = textBody,
    iconMuted = iconMuted,
    accent = accent,
    onAccent = onAccent,
    accentSoft = accentSoft,
    card = card,
    cardBorder = cardBorder,
    divider = divider,
    outline = outline,
    subtleFill = subtleFill,
    inverse = text,
    onInverse = onInverse,
    selected = selected,
    selectedBorder = accent,
    disabled = disabled,
    onDisabled = onDisabled,
    switchThumbOff = switchThumbOff,
    healthy = tones[0],
    mutant = tones[1],
    neutral = tones[2],
    resistant = tones[3],
    accentTone = tones[4],
)

/** design.html: light screens with navy secret areas. */
val LightColorScheme = SporzColorScheme(
    isDark = false,
    standard = colors(
        kind = SurfaceKind.Standard, tones = LightTones,
        background = SporzPalette.Paper, text = SporzPalette.Navy,
        textMuted = SporzPalette.Muted, textBody = SporzPalette.Slate, iconMuted = SporzPalette.Grey, onDisabled = SporzPalette.Slate,
        accent = SporzPalette.Orange, onAccent = SporzPalette.White, accentSoft = SporzPalette.OrangeSoft,
        card = SporzPalette.White, cardBorder = SporzPalette.Line, divider = SporzPalette.LineSoft,
        outline = SporzPalette.LineStrong, subtleFill = SporzPalette.Paper, onInverse = SporzPalette.White,
        selected = SporzPalette.OrangeTint,
        disabled = SporzPalette.Line, switchThumbOff = SporzPalette.White,
    ),
    secret = colors(
        kind = SurfaceKind.Secret, tones = DarkTones,
        background = SporzPalette.Navy, text = SporzPalette.OffWhite,
        textMuted = SporzPalette.MutedOnNavy, textBody = SporzPalette.BodyOnNavy, iconMuted = SporzPalette.Grey,
        accent = SporzPalette.OrangeLight, onAccent = SporzPalette.Navy, accentSoft = SporzPalette.NavySelected,
        card = SporzPalette.NavyCard, cardBorder = SporzPalette.NavyLine, divider = SporzPalette.NavyPill,
        outline = SporzPalette.NavyLineStrong, subtleFill = SporzPalette.NavyLine, onInverse = SporzPalette.Navy,
        selected = SporzPalette.NavySelected,
        disabled = SporzPalette.NavyLine, switchThumbOff = SporzPalette.MutedOnNavy,
    ),
    inset = colors(
        kind = SurfaceKind.Inset, tones = LightTones,
        background = SporzPalette.OffWhite, text = SporzPalette.Navy,
        textMuted = SporzPalette.Muted, textBody = SporzPalette.Slate, iconMuted = SporzPalette.Grey, onDisabled = SporzPalette.Slate,
        accent = SporzPalette.Orange, onAccent = SporzPalette.White, accentSoft = SporzPalette.OrangeSoft,
        card = SporzPalette.White, cardBorder = SporzPalette.Line, divider = SporzPalette.LineSoft,
        outline = SporzPalette.LineStrong, subtleFill = SporzPalette.Paper, onInverse = SporzPalette.White,
        selected = SporzPalette.OrangeTint,
        disabled = SporzPalette.Line, switchThumbOff = SporzPalette.White,
    ),
)

/** Everything dark; secret areas are darker still so they keep standing out. */
val DarkColorScheme = SporzColorScheme(
    isDark = true,
    standard = colors(
        kind = SurfaceKind.Standard, tones = DarkTones,
        background = SporzPalette.NightBackground, text = SporzPalette.OffWhite,
        textMuted = SporzPalette.NightMuted, textBody = SporzPalette.BodyOnNavy, iconMuted = SporzPalette.NightIcon,
        accent = SporzPalette.OrangeLight, onAccent = SporzPalette.Navy, accentSoft = SporzPalette.NavySelected,
        card = SporzPalette.NightCard, cardBorder = SporzPalette.NightLine, divider = SporzPalette.NightDivider,
        outline = SporzPalette.NavyLineStrong, subtleFill = SporzPalette.NightFill, onInverse = SporzPalette.Navy,
        selected = SporzPalette.NightSelected,
        disabled = SporzPalette.NightFill, switchThumbOff = SporzPalette.MutedOnNavy,
    ),
    secret = colors(
        kind = SurfaceKind.Secret, tones = DarkTones,
        background = SporzPalette.SecretBackground, text = SporzPalette.OffWhite,
        textMuted = SporzPalette.NightMuted, textBody = SporzPalette.BodyOnNavy, iconMuted = SporzPalette.NightIcon,
        accent = SporzPalette.OrangeLight, onAccent = SporzPalette.Navy, accentSoft = SporzPalette.NavySelected,
        card = SporzPalette.SecretCard, cardBorder = SporzPalette.NightDivider, divider = SporzPalette.NightCard,
        outline = SporzPalette.SecretLine, subtleFill = SporzPalette.NightFill, onInverse = SporzPalette.Navy,
        selected = SporzPalette.NightSelected,
        disabled = SporzPalette.NightCard, switchThumbOff = SporzPalette.MutedOnNavy,
    ),
    inset = colors(
        kind = SurfaceKind.Inset, tones = DarkTones,
        background = SporzPalette.NightFill, text = SporzPalette.OffWhite,
        textMuted = SporzPalette.NightMuted, textBody = SporzPalette.BodyOnNavy, iconMuted = SporzPalette.NightIcon,
        accent = SporzPalette.OrangeLight, onAccent = SporzPalette.Navy, accentSoft = SporzPalette.NavySelected,
        card = SporzPalette.NightCard, cardBorder = SporzPalette.NavyLineStrong, divider = SporzPalette.NightLine,
        outline = SporzPalette.NavyLineStrong, subtleFill = SporzPalette.NightLine, onInverse = SporzPalette.Navy,
        selected = SporzPalette.NightSelected,
        disabled = SporzPalette.NightLine, switchThumbOff = SporzPalette.MutedOnNavy,
    ),
)

val LocalSporzColorScheme = staticCompositionLocalOf { LightColorScheme }
val LocalSporzColors = staticCompositionLocalOf { LightColorScheme.standard }

/** Colors of this tone on the current surface. */
val Tone.colors: ToneColors
    @Composable @ReadOnlyComposable get() = LocalSporzColors.current.tone(this)
