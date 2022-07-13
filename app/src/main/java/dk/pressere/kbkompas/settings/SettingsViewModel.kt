package dk.pressere.kbkompas.settings

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel(private val preferences: SharedPreferences) : ViewModel() {

    val darkMode = mutableStateOf(preferences.getBoolean(DARK_MODE, false))
    val devMode = mutableStateOf(preferences.getBoolean(DEV_MODE, false))
    var devModeDialogShown = preferences.getBoolean(DEV_MODE_DIALOG, false)
        set(value) {
            preferences.edit().putBoolean(DEV_MODE_DIALOG, value).apply()
            field = value
        }

    fun setDarkMode(b : Boolean) {
        preferences.edit().putBoolean(DARK_MODE, b).apply()
        darkMode.value = b
    }

    fun setDevMode(b : Boolean) {
        preferences.edit().putBoolean(DEV_MODE, b).apply()
        devMode.value = b
    }

    fun resetAll() {
        setDarkMode(false)
        setDevMode(false)
        devModeDialogShown = false
    }

    companion object {
        private const val DARK_MODE = "dark_mode"
        private const val DEV_MODE = "dev_mode"
        private const val DEV_MODE_DIALOG = "dev_mode_dialog"
    }
}