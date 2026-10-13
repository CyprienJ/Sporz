package io.github.cyprienj.sporz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.cyprienj.sporz.navigation.SporzNavHost
import io.github.cyprienj.sporz.ui.theme.SporzTheme

// Single activity: every screen is a Compose destination of SporzNavHost.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SporzTheme {
                SporzNavHost()
            }
        }
    }
}
