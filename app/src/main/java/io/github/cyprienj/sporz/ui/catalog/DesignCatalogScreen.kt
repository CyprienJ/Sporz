package io.github.cyprienj.sporz.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.components.ActionCard
import io.github.cyprienj.sporz.ui.components.Avatar
import io.github.cyprienj.sporz.ui.components.CampBar
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.FilterChip
import io.github.cyprienj.sporz.ui.components.GameCodeField
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.InfoNote
import io.github.cyprienj.sporz.ui.components.Overline
import io.github.cyprienj.sporz.ui.components.PlayerRow
import io.github.cyprienj.sporz.ui.components.RadioCard
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SegmentedControl
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzCard
import io.github.cyprienj.sporz.ui.components.SporzTabBar
import io.github.cyprienj.sporz.ui.components.SporzTextField
import io.github.cyprienj.sporz.ui.components.StatTile
import io.github.cyprienj.sporz.ui.components.StatusBadge
import io.github.cyprienj.sporz.ui.components.StepIndicator
import io.github.cyprienj.sporz.ui.components.StepState
import io.github.cyprienj.sporz.ui.components.Stepper
import io.github.cyprienj.sporz.ui.components.TabItem
import io.github.cyprienj.sporz.ui.components.TargetChip
import io.github.cyprienj.sporz.ui.components.ToggleRow
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.InsetSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone
import io.github.cyprienj.sporz.ui.theme.colors
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * Debug-only showcase of the design system (ticket A4), with the sample data
 * of design.html. Reached from the home screen in debug builds. The sample
 * texts are not app strings, so they are not in strings.xml.
 */
@Composable
fun DesignCatalogScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    // 0 = follow the system, 1 = light, 2 = dark: lets you compare both themes.
    var themeChoice by remember { mutableIntStateOf(0) }
    val dark = when (themeChoice) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    SporzTheme(darkTheme = dark) {
        CatalogContent(themeChoice, { themeChoice = it }, onBack, modifier)
    }
}

@Composable
private fun CatalogContent(
    themeChoice: Int,
    onThemeChoice: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        ScreenHeader(
            title = stringResource(R.string.screen_design_catalog),
            overline = "Ticket A4",
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CatalogSection("Thème") {
                SegmentedControl(listOf("Système", "Clair", "Sombre"), themeChoice, onThemeChoice)
            }
            StandardSamples()
            SecretSurface(Modifier.fillMaxWidth()) { SecretSamples() }
        }
        var tab by remember { mutableIntStateOf(0) }
        SporzTabBar(
            items = listOf(
                TabItem(stringResource(R.string.tab_phase), SporzIcons.Night),
                TabItem(stringResource(R.string.tab_players), SporzIcons.Players),
                TabItem(stringResource(R.string.tab_journal), SporzIcons.Journal),
            ),
            selectedIndex = tab,
            onSelect = { tab = it },
        )
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        content()
    }
}

@Composable
private fun StandardSamples() {
    CatalogSection("Typographie") {
        Text("SPORZ", style = SporzTypography.display)
        Text("GÉNÉTICIEN", style = SporzTypography.roleTitle)
        Text("COMMENT JOUEZ-VOUS ?", style = SporzTypography.title)
        Text("MÉDECINS", style = SporzTypography.heading)
        Text("Chaque nuit, désigne un joueur : le MJ te révèle son génome.", style = SporzTypography.bodyLarge)
        Text("Les autres joueurs ont un génome normal.", style = SporzTypography.small, color = SporzTheme.colors.textMuted)
    }
    CatalogSection("Boutons") {
        SporzButton("Ouvrir le lobby", onClick = {}, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SporzButton("Suggérer", onClick = {}, style = SporzButtonStyle.Secondary)
            SporzButton("Soigner", onClick = {}, style = SporzButtonStyle.Inverse, modifier = Modifier.weight(1f))
        }
        SporzButton("Masquer mon rôle", onClick = {}, icon = SporzIcons.Hide, modifier = Modifier.fillMaxWidth())
        SporzButton("Choisis un joueur", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
    }
    CatalogSection("Cartes d'action") {
        ActionCard("Créer une partie", "Je suis le MJ", SporzIcons.Plus, onClick = {}, primary = true)
        ActionCard("Rejoindre une partie", "Avec un code ou un QR", SporzIcons.Join, onClick = {})
    }
    CatalogSection("Choix") {
        var mode by remember { mutableIntStateOf(1) }
        RadioCard("Hors ligne", "Un seul téléphone. Vous saisissez les noms et annoncez chaque rôle en privé.", mode == 0, { mode = 0 })
        RadioCard("Semi-online", "Certains rejoignent avec leur téléphone, les autres sont ajoutés à la main.", mode == 1, { mode = 1 })
        var action by remember { mutableIntStateOf(0) }
        SegmentedControl(listOf("Soigner", "Tuer"), action, { action = it })
        InfoNote("Le MJ ne joue pas. Vous pourrez ajouter des joueurs hors ligne à tout moment avant la distribution.")
    }
    CatalogSection("Compteurs et réglages") {
        var doctors by remember { mutableIntStateOf(2) }
        SporzCard(contentPadding = PaddingValues(start = 14.dp, top = 6.dp, end = 6.dp, bottom = 6.dp)) {
            Row(
                Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CampBar(Tone.Healthy.colors.strong, Modifier.padding(vertical = 6.dp))
                Column(Modifier.weight(1f)) {
                    Text("Médecin", style = SporzTypography.bodyStrong)
                    Text("Soigne ou tue", style = SporzTypography.small, color = SporzTheme.colors.textMuted)
                }
                Stepper(
                    value = "$doctors",
                    onDecrement = { doctors-- },
                    onIncrement = { doctors++ },
                    decrementLabel = "Retirer un Médecin",
                    incrementLabel = "Ajouter un Médecin",
                    canDecrement = doctors > 1,
                    canIncrement = doctors < 3,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("1", "Hôtes", Modifier.weight(1f))
            StatTile("1", "Résistants", Modifier.weight(1f))
        }
        var reports by remember { mutableStateOf(true) }
        var vote by remember { mutableStateOf(false) }
        CardGroup {
            ToggleRow("Envoi automatique des rapports", reports, { reports = it }, hint = "Le résultat arrive sur le téléphone sans validation du MJ.")
            GroupDivider()
            ToggleRow("Vote sur téléphone", vote, { vote = it }, hint = "Sinon le MJ saisit tous les votes à la main.")
        }
    }
    CatalogSection("Joueurs") {
        var filter by remember { mutableIntStateOf(0) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tous", "Vivants", "Mutés", "Hors ligne", "Éjectés").forEachIndexed { i, label ->
                FilterChip(label, filter == i, { filter = i })
            }
        }
        CardGroup {
            PlayerRow("Alice", leading = { Avatar("Alice") }, trailing = { Text("Téléphone", style = SporzTypography.small, color = SporzTheme.colors.textMuted) })
            GroupDivider()
            PlayerRow("Bastien", leading = { Avatar("Bastien", offline = true) }, trailing = { Text("Hors ligne", style = SporzTypography.small, color = SporzTheme.colors.textMuted) })
            GroupDivider()
            PlayerRow("Gaëlle", subtitle = "Mutant de base · génome hôte", offline = true, leading = { CampBar(Tone.Mutant.colors.strong) }, trailing = { StatusBadge("Muté", Tone.Mutant) })
            GroupDivider()
            PlayerRow("Farid", subtitle = "Espion · génome résistant", leading = { CampBar(Tone.Healthy.colors.strong) }, trailing = { StatusBadge("Sain", Tone.Healthy) })
            GroupDivider()
            PlayerRow("Hugo", subtitle = "Astronaute · génome normal", eliminated = true, leading = { CampBar(Tone.Neutral.colors.strong) }, trailing = { StatusBadge("Éjecté", Tone.Neutral) })
        }
        SporzCard(highlighted = true) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepIndicator(2, StepState.Current)
                Text("MÉDECINS", style = SporzTypography.heading, modifier = Modifier.weight(1f))
                Text("Bastien · Emma", style = SporzTypography.label, color = SporzTheme.colors.textMuted)
            }
            var target by remember { mutableStateOf("Chloé") }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Alice", "Bastien", "Chloé", "Dorian", "Hugo").forEach { name ->
                    TargetChip(name, target == name, { target = name }, enabled = name != "Hugo")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            StepIndicator(1, StepState.Done)
            StepIndicator(3, StepState.Upcoming)
            StatusBadge("Résistant", Tone.Resistant)
            StatusBadge("Annoncé", Tone.Accent)
        }
    }
    CatalogSection("Saisie") {
        var code by remember { mutableStateOf("KX7Q") }
        var name by remember { mutableStateOf("Jules") }
        GameCodeField(code, { code = it }, label = "Code de partie")
        SporzTextField(name, { name = it }, label = "Ton prénom", supportingText = "C'est le nom que verront le MJ et les autres joueurs.")
    }
}

@Composable
private fun SecretSamples() {
    CatalogSection("Surface secrète") {
        Overline("Ton rôle")
        Text("GÉNÉTICIEN", style = SporzTypography.roleTitle)
        var preset by remember { mutableIntStateOf(1) }
        SegmentedControl(listOf("Manuel", "Assisté", "Automatique"), preset, { preset = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("7", "sains", Modifier.weight(1f), valueColor = Tone.Healthy.colors.content)
            StatTile("2", "camp mutant", Modifier.weight(1f), valueColor = Tone.Mutant.colors.content)
            StatTile("1", "éjecté", Modifier.weight(1f))
        }
        var picked by remember { mutableStateOf<String?>(null) }
        listOf("Farid", "Gaëlle").forEach { name ->
            SporzCard(highlighted = picked == name, contentPadding = PaddingValues(0.dp)) {
                PlayerRow(
                    name,
                    onClick = { picked = name },
                    leading = { Avatar(name) },
                    trailing = { if (name == "Farid") Text("Résistant (N1)", style = SporzTypography.small, color = SporzTheme.colors.accent) },
                )
            }
        }
        SporzButton(if (picked != null) "Analyser $picked" else "Choisis un joueur", onClick = {}, enabled = picked != null, modifier = Modifier.fillMaxWidth())
        InfoNote("Ajouté à ton carnet, avec Farid : résistant (nuit 1).", icon = SporzIcons.Journal)
        InsetSurface(Modifier.fillMaxWidth(), shape = SporzShapes.large) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Overline("Résultat")
                Text("GAËLLE : HÔTE", style = SporzTypography.titleLarge.copy(fontSize = SporzTypography.titleLarge.fontSize * 0.9f))
            }
        }
        SporzButton("Masquer l'écran", onClick = {}, style = SporzButtonStyle.Secondary, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(heightDp = 2400)
@Composable
private fun DesignCatalogPreview() {
    SporzTheme { DesignCatalogScreen(onBack = {}) }
}
