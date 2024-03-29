package dk.pressere.kbkompas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.pressere.kbkompas.settings.SettingsViewModel
import dk.pressere.kbkompas.settings.SettingsViewModelFactory
import dk.pressere.kbkompas.ui.theme.BarFinder2Theme

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences = getPreferences(MODE_PRIVATE)
            val settingsViewModel: SettingsViewModel = viewModel(factory =  SettingsViewModelFactory(preferences) )
            BarFinder2Theme(settingsViewModel.darkMode.value) {
                ScaffoldFrame(settingsViewModel)
            }
        }
    }
}