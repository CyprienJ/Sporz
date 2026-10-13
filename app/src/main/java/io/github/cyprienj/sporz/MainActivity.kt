package io.github.cyprienj.sporz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.cyprienj.sporz.navigation.SporzNavHost

// Single activity: every screen is a Compose destination of SporzNavHost.
// MaterialTheme is replaced by the Sporz design system in ticket A4.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                SporzNavHost()
            }
        }
    }
}
