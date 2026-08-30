package io.github.lucf15.restorecredentials.platform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppThemeProvider
import io.github.lucf15.restorecredentials.ui.shell.RestoreCredentialsApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent { AppThemeProvider { RestoreCredentialsApp() } }
    }
}
