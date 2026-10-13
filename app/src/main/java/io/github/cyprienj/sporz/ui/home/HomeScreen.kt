package io.github.cyprienj.sporz.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.components.ActionCard
import io.github.cyprienj.sporz.ui.components.TextLink
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.StandardSurface

/** design.html, board "Accueil". */
@Composable
fun HomeScreen(
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    SecretSurface(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Brand(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            Actions(onCreateGame, onJoinGame, onOpenRoleGuide, onOpenRecentGames, onOpenDesignCatalog)
        }
    }
}

@Composable
private fun Brand(modifier: Modifier = Modifier) {
    val colors = SporzTheme.colors
    Column(
        modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState(), reverseScrolling = true)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom),
    ) {
        Icon(SporzIcons.Logo, contentDescription = null, tint = colors.accent, modifier = Modifier.size(64.dp))
        Text(
            stringResource(R.string.app_name).uppercase(),
            style = SporzTypography.display,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.home_tagline),
            style = SporzTypography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
            color = colors.textMuted,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

@Composable
private fun Actions(
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
) {
    StandardSurface(Modifier.fillMaxWidth(), shape = SporzShapes.sheet) {
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionCard(
                title = stringResource(R.string.action_create_game),
                subtitle = stringResource(R.string.home_create_subtitle),
                icon = SporzIcons.Plus,
                onClick = onCreateGame,
                primary = true,
            )
            ActionCard(
                title = stringResource(R.string.action_join_game),
                subtitle = stringResource(R.string.home_join_subtitle),
                icon = SporzIcons.Join,
                onClick = onJoinGame,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextLink(stringResource(R.string.action_rules_and_roles), onOpenRoleGuide)
                TextLink(stringResource(R.string.screen_recent_games), onOpenRecentGames)
            }
            if (onOpenDesignCatalog != null) {
                TextLink(
                    stringResource(R.string.screen_design_catalog),
                    onOpenDesignCatalog,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Preview(name = "Light")
@Composable
private fun HomeLightPreview() {
    SporzTheme(darkTheme = false) { HomeScreen({}, {}, {}, {}, onOpenDesignCatalog = null) }
}

@Preview(name = "Dark")
@Composable
private fun HomeDarkPreview() {
    SporzTheme(darkTheme = true) { HomeScreen({}, {}, {}, {}, onOpenDesignCatalog = {}) }
}
