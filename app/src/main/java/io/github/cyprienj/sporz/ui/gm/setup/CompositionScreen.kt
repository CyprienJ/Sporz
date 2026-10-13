package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.setup.CompositionCheck
import io.github.cyprienj.sporz.engine.setup.CompositionProblem
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import io.github.cyprienj.sporz.engine.setup.check
import io.github.cyprienj.sporz.ui.common.hintRes
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.common.startTone
import io.github.cyprienj.sporz.ui.components.CampBar
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzCard
import io.github.cyprienj.sporz.ui.components.Stepper
import io.github.cyprienj.sporz.ui.components.ToggleRow
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.colors

/** Actions of the composition screen, so the screen stays stateless. */
data class CompositionActions(
    val canAdd: (Role) -> Boolean,
    val canRemove: (Role) -> Boolean,
    val add: (Role) -> Unit,
    val remove: (Role) -> Unit,
    val canAddGenome: Boolean,
    val addHost: () -> Unit,
    val removeHost: () -> Unit,
    val addResistant: () -> Unit,
    val removeResistant: () -> Unit,
    val setVariants: (Variants) -> Unit,
    val suggest: () -> Unit,
)

/** design.html, board "MJ · Composition des rôles". */
@Composable
fun CompositionRoute(
    onBack: () -> Unit,
    onDealt: () -> Unit,
    viewModel: CompositionViewModel = hiltViewModel(),
) {
    val composition by viewModel.composition.collectAsStateWithLifecycle()
    CompositionScreen(
        composition = composition,
        check = composition.check(viewModel.playerCount),
        actions = CompositionActions(
            canAdd = viewModel::canAdd,
            canRemove = viewModel::canRemove,
            add = viewModel::add,
            remove = viewModel::remove,
            canAddGenome = viewModel.canAddGenome(),
            addHost = viewModel::addHost,
            removeHost = viewModel::removeHost,
            addResistant = viewModel::addResistant,
            removeResistant = viewModel::removeResistant,
            setVariants = viewModel::setVariants,
            suggest = viewModel::suggest,
        ),
        onBack = onBack,
        onDeal = { if (viewModel.deal()) onDealt() },
    )
}

@Composable
fun CompositionScreen(
    composition: Composition,
    check: CompositionCheck,
    actions: CompositionActions,
    onBack: () -> Unit,
    onDeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SporzTheme.colors
    val totalShown = composition.chosenRoleCount + check.astronauts.coerceAtLeast(0)
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        ScreenHeader(
            title = stringResource(R.string.composition_title),
            overline = stringResource(R.string.composition_step),
            onBack = onBack,
        ) {
            Text(
                pluralStringResource(R.plurals.composition_total, check.playerCount, totalShown, check.playerCount),
                style = SporzTypography.number,
                color = if (check.isValid) colors.healthy.strong else colors.mutant.strong,
            )
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Role.entries.forEach { role ->
                    RoleRow(
                        role = role,
                        count = if (role == Role.Astronaut) check.astronauts.coerceAtLeast(0) else composition.count(role),
                        actions = actions,
                    )
                }
            }
            Genomes(composition, actions)
            VariantsSection(composition.variants, actions.setVariants)
        }
        Footer(check, actions.suggest, onDeal)
    }
}

@Composable
private fun RoleRow(role: Role, count: Int, actions: CompositionActions) {
    val name = stringResource(role.nameRes)
    SporzCard(contentPadding = PaddingValues(start = 14.dp, top = 6.dp, end = 6.dp, bottom = 6.dp)) {
        Row(
            Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CampBar(role.startTone.colors.strong, Modifier.padding(vertical = 6.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = SporzTypography.bodyStrong)
                Text(stringResource(role.hintRes), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
            }
            // Base mutant (always 1) and astronauts (computed) have no stepper, as in the design.
            if (role == Role.BaseMutant || role == Role.Astronaut) {
                Text("$count", style = SporzTypography.number, textAlign = TextAlign.Center, modifier = Modifier.width(132.dp))
            } else {
                Stepper(
                    value = "$count",
                    onDecrement = { actions.remove(role) },
                    onIncrement = { actions.add(role) },
                    decrementLabel = stringResource(R.string.composition_less, name),
                    incrementLabel = stringResource(R.string.composition_more, name),
                    canDecrement = actions.canRemove(role),
                    canIncrement = actions.canAdd(role),
                )
            }
        }
    }
}

@Composable
private fun Genomes(composition: Composition, actions: CompositionActions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.composition_genomes))
        CardGroup {
            GenomeRow(
                label = stringResource(R.string.composition_hosts),
                extra = stringResource(R.string.composition_hosts_extra),
                count = composition.extraHosts,
                onDecrement = actions.removeHost,
                onIncrement = actions.addHost,
                canDecrement = composition.extraHosts > 0,
                canIncrement = actions.canAddGenome,
            )
            GroupDivider()
            GenomeRow(
                label = stringResource(R.string.composition_resistants),
                extra = null,
                count = composition.resistants,
                onDecrement = actions.removeResistant,
                onIncrement = actions.addResistant,
                canDecrement = composition.resistants > 0,
                canIncrement = actions.canAddGenome,
            )
        }
        Text(stringResource(R.string.composition_genomes_note), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
    }
}

@Composable
private fun GenomeRow(
    label: String,
    extra: String?,
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean,
) {
    Row(
        Modifier.padding(start = 14.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = SporzTypography.bodyStrong)
            if (extra != null) Text(extra, style = SporzTypography.small, color = SporzTheme.colors.textMuted)
        }
        Stepper(
            value = "$count",
            onDecrement = onDecrement,
            onIncrement = onIncrement,
            decrementLabel = stringResource(R.string.composition_less, label),
            incrementLabel = stringResource(R.string.composition_more, label),
            canDecrement = canDecrement,
            canIncrement = canIncrement,
        )
    }
}

@Composable
private fun VariantsSection(variants: Variants, onChange: (Variants) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.composition_variants))
        CardGroup {
            ToggleRow(stringResource(R.string.variant_hacker_can_hack_spy), variants.hackerCanHackSpy, { onChange(variants.copy(hackerCanHackSpy = it)) })
            GroupDivider()
            ToggleRow(stringResource(R.string.variant_reveal_on_elimination), variants.revealOnElimination, { onChange(variants.copy(revealOnElimination = it)) })
            GroupDivider()
            ToggleRow(stringResource(R.string.variant_blank_vote), variants.blankVoteAllowed, { onChange(variants.copy(blankVoteAllowed = it)) })
            GroupDivider()
            ToggleRow(stringResource(R.string.variant_captain), variants.captain, { onChange(variants.copy(captain = it)) })
        }
    }
}

@Composable
private fun Footer(check: CompositionCheck, onSuggest: () -> Unit, onDeal: () -> Unit) {
    val colors = SporzTheme.colors
    Column(Modifier.background(colors.background)) {
        HorizontalDivider(color = colors.cardBorder)
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            check.problems.forEach { problem ->
                Text(
                    problem.message(),
                    style = SporzTypography.small.copy(fontWeight = if (problem.isError) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (problem.isError) colors.mutant.content else colors.textMuted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SporzButton(stringResource(R.string.action_suggest), onSuggest, style = SporzButtonStyle.Secondary)
                SporzButton(stringResource(R.string.action_deal), onDeal, enabled = check.isValid, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CompositionProblem.message(): String = when (this) {
    is CompositionProblem.PlayerCountOutOfRange -> stringResource(R.string.composition_error_player_count, min, max)
    is CompositionProblem.RoleCountOutOfRange -> stringResource(R.string.composition_error_role_count, stringResource(role.nameRes), min, max)
    is CompositionProblem.TooManyRoles -> pluralStringResource(R.plurals.composition_error_too_many_roles, missingSeats, missingSeats)
    CompositionProblem.NegativeGenomeCount -> stringResource(R.string.composition_error_negative_genomes)
    is CompositionProblem.TooManyGenomes -> stringResource(R.string.composition_error_too_many_genomes, available)
    CompositionProblem.HackerWithoutTarget -> stringResource(R.string.composition_warning_hacker)
}

private val previewActions = CompositionActions({ true }, { true }, {}, {}, true, {}, {}, {}, {}, {}, {})

@Preview(name = "Light, design composition", heightDp = 1400)
@Composable
private fun CompositionLightPreview() {
    val composition = SuggestedCompositions.forPlayers(10)
    SporzTheme(darkTheme = false) { CompositionScreen(composition, composition.check(10), previewActions, {}, {}) }
}

@Preview(name = "Dark, too many roles")
@Composable
private fun CompositionDarkPreview() {
    val composition = SuggestedCompositions.forPlayers(10)
    SporzTheme(darkTheme = true) { CompositionScreen(composition, composition.check(8), previewActions, {}, {}) }
}
