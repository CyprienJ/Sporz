package io.github.cyprienj.sporz.ui.guide

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.Role as GameRole
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.components.SporzIconButton
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzPalette
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone
import io.github.cyprienj.sporz.ui.theme.colors

/** A card of the guide; the bar color comes from [tone], or [barColor] for game phases. */
private data class GuideCard(
    @param:StringRes val name: Int,
    @param:StringRes val tag: Int,
    @param:StringRes val body: Int,
    val tone: Tone? = null,
    val barColor: (@Composable () -> Color)? = null,
)

private val roles = listOf(
    GuideCard(GameRole.BaseMutant.nameRes, R.string.guide_tag_mutant, R.string.guide_base_mutant, Tone.Mutant),
    GuideCard(GameRole.Doctor.nameRes, R.string.guide_tag_every_night, R.string.guide_doctor, Tone.Healthy),
    GuideCard(GameRole.Psychologist.nameRes, R.string.guide_tag_every_night, R.string.guide_psychologist, Tone.Healthy),
    GuideCard(GameRole.Geneticist.nameRes, R.string.guide_tag_every_night, R.string.guide_geneticist, Tone.Healthy),
    GuideCard(GameRole.ComputerScientist.nameRes, R.string.guide_tag_every_night, R.string.guide_computer_scientist, Tone.Healthy),
    GuideCard(GameRole.Hacker.nameRes, R.string.guide_tag_every_night, R.string.guide_hacker, Tone.Healthy),
    GuideCard(GameRole.Spy.nameRes, R.string.guide_tag_every_night, R.string.guide_spy, Tone.Healthy),
    GuideCard(GameRole.Traitor.nameRes, R.string.guide_tag_traitor, R.string.guide_traitor, Tone.Mutant),
    GuideCard(GameRole.Astronaut.nameRes, R.string.guide_tag_astronaut, R.string.guide_astronaut, Tone.Healthy),
)

private val genomes = listOf(
    GuideCard(R.string.guide_host, R.string.guide_host_tag, R.string.guide_host_body, Tone.Mutant),
    GuideCard(R.string.guide_normal, R.string.guide_normal_tag, R.string.guide_normal_body, Tone.Neutral),
    GuideCard(R.string.guide_resistant, R.string.guide_resistant_tag, R.string.guide_resistant_body, Tone.Resistant),
)

private val game = listOf(
    GuideCard(R.string.guide_night, R.string.guide_night_tag, R.string.guide_night_body, barColor = { SporzTheme.colors.text }),
    GuideCard(R.string.guide_day, R.string.guide_day_tag, R.string.guide_day_body, barColor = { SporzPalette.OrangeLight }),
    GuideCard(R.string.guide_vote, R.string.guide_vote_tag, R.string.guide_vote_body, barColor = { SporzTheme.colors.accent }),
    GuideCard(R.string.guide_victory, R.string.guide_victory_tag, R.string.guide_victory_body, Tone.Healthy),
)

private val tabs = listOf(R.string.guide_tab_roles to roles, R.string.guide_tab_genomes to genomes, R.string.guide_tab_game to game)

/** design.html, board "Guide des rôles". Reachable from the home screen and the waiting room. */
@Composable
fun RoleGuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    LightStatusBarIcons()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var open by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(Modifier.offset(x = (-12).dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SporzIconButton(SporzIcons.Back, stringResource(R.string.action_back), onBack)
                    Text(stringResource(R.string.guide_title).uppercase(), style = SporzTypography.titleSmall, modifier = Modifier.semantics { heading() })
                }
                Tabs(tab) {
                    tab = it
                    open = null
                }
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tabs[tab].second, key = { it.name }) { card ->
                Card(card, expanded = open == card.name, onToggle = { open = if (open == card.name) null else card.name })
            }
        }
    }
}

/** Underlined tabs of the design (orange line and text when selected). */
@Composable
private fun Tabs(selected: Int, onSelect: (Int) -> Unit) {
    val colors = SporzTheme.colors
    Row(Modifier.fillMaxWidth().selectableGroup()) {
        tabs.forEachIndexed { index, (label, _) ->
            val isSelected = index == selected
            Column(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .selectable(isSelected, role = Role.Tab, onClick = { onSelect(index) }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(label),
                        style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSelected) colors.accent else colors.textMuted,
                    )
                }
                Box(Modifier.fillMaxWidth().height(3.dp).background(if (isSelected) colors.accent else Color.Transparent))
            }
        }
    }
}

@Composable
private fun Card(card: GuideCard, expanded: Boolean, onToggle: () -> Unit) {
    val colors = SporzTheme.colors
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    val bar = card.barColor?.invoke() ?: card.tone!!.colors.strong
    val state = stringResource(if (expanded) R.string.state_expanded else R.string.state_collapsed)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(SporzShapes.large)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, SporzShapes.large)
            .animateContentSize(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(role = Role.Button, onClick = onToggle)
                .semantics { stateDescription = state }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(bar, RoundedCornerShape(2.dp)))
            Column(Modifier.weight(1f)) {
                Text(stringResource(card.name).uppercase(), style = SporzTypography.heading.copy(fontSize = 21.sp))
                Text(stringResource(card.tag), style = SporzTypography.small, color = colors.textMuted)
            }
            Icon(SporzIcons.ChevronDown, contentDescription = null, modifier = Modifier.size(20.dp).rotate(rotation))
        }
        if (expanded) {
            Text(
                stringResource(card.body),
                style = SporzTypography.body.copy(lineHeight = 23.sp),
                color = colors.textBody,
                modifier = Modifier.padding(start = 30.dp, end = 14.dp, bottom = 14.dp),
            )
        }
    }
}
