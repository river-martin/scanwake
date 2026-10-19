package io.github.river_martin.scanwake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.river_martin.scanwake.ui.navigation.ScanWakeNavGraph
import io.github.river_martin.scanwake.ui.theme.ScanWakeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            _root_ide_package_.io.github.river_martin.scanwake.ui.theme.ScanWakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    _root_ide_package_.io.github.river_martin.scanwake.ui.navigation.ScanWakeNavGraph()
                }
            }
        }
    }
}
