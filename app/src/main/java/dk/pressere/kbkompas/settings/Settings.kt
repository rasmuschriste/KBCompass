package dk.pressere.kbkompas.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dk.pressere.kbkompas.R
import dk.pressere.kbkompas.compass.CompassViewModel
import dk.pressere.kbkompas.components.DefaultDialog
import dk.pressere.kbkompas.ui.theme.CompassBackgroundDark
import dk.pressere.kbkompas.ui.theme.CompassBackgroundLight
import dk.pressere.kbkompas.ui.theme.Primary
import java.security.MessageDigest


@Composable
fun SettingsContent(compassViewModel: CompassViewModel, settingsViewModel: SettingsViewModel) {
    val showDevModeDialog = remember { mutableStateOf(false) }
    val showDeleteAchievementDialog = remember { mutableStateOf(false) }
    val showCheatCodeDialog = remember { mutableStateOf(false) }

    LazyColumn {
        item {
            SettingsElement(
                title = "Dark Theme",
                desc = stringResource(id = R.string.action_toggle_dark_mode),
                iconId = R.drawable.ic_baseline_brightness_6_24,
                onClick = {
                    settingsViewModel.setDarkMode(!settingsViewModel.darkMode.value)
                },
                extraInterface = {
                    ToggleIconReactor(settingsViewModel.darkMode.value)
                }
            )
        }
        item {
            SettingsElement(
                title = "Slet bedriftdata",
                desc = "Slet data brugt af bedrifter",
                iconId = R.drawable.ic_baseline_delete_24,
                onClick = {
                    showDeleteAchievementDialog.value = true
                }
            )
        }
        item {
            SettingsElement(
                title = "Udviklertilstand",
                desc = "Skift til udviklertilstand",
                iconId = R.drawable.ic_baseline_plumbing_24,
                onClick = {
                    if (!settingsViewModel.devModeDialogShown && !settingsViewModel.devMode.value) {
                        showDevModeDialog.value = true
                    } else {
                        settingsViewModel.setDevMode(!settingsViewModel.devMode.value)
                    }
                },
                extraInterface = {
                    ToggleIconReactor(settingsViewModel.devMode.value)
                }
            )
        }

        // Developer Options
        if (settingsViewModel.devMode.value) {
            item {
                Divider(modifier = Modifier.padding(top = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        text = "Developer actions",
                        fontSize = 16.sp
                    )
                }
            }
            item {
                SettingsElement(
                    title = "Reset Data",
                    desc = "Reset most data values",
                    iconId = R.drawable.ic_baseline_plumbing_24,
                    onClick = {
                        settingsViewModel.resetAll()
                    }
                )
            }
            item {
                SettingsElement(
                    title = "Forget Destinations",
                    desc = "Delete local destination preferences",
                    iconId = R.drawable.ic_baseline_plumbing_24,
                    onClick = {
                        compassViewModel.purgeDestinationData()
                    }
                )
            }
            item {
                SettingsElement(
                    title = "Cheats",
                    desc = "Unlock achievements with codes",
                    iconId = R.drawable.ic_baseline_plumbing_24,
                    onClick = {
                        showCheatCodeDialog.value = true
                    }
                )
            }

        }


    }


    // Show dialogs.
    DefaultDialog(
        showDialog = showDevModeDialog.value,
        message = "Udviklertilstand er ikke testet og gør det muligt at ændre data, så applikationen stopper med at virke. Skal udvikler tilstand slås til?",
        onDismiss = { showDevModeDialog.value = false },
        dismiss = "Nej",
        onAction = {
            showDevModeDialog.value = false
            settingsViewModel.devModeDialogShown = true
            settingsViewModel.setDevMode(!settingsViewModel.devMode.value)
        },
        action = "Ja"
    )

    DefaultDialog(
        showDialog = showDeleteAchievementDialog.value,
        message = "Er du sikker på du vil slette alt data tilhørende bedrifter? Dette vil fjerne fremskridt for alle bedrifter",
        onDismiss = { showDeleteAchievementDialog.value = false },
        dismiss = "Nej",
        onAction = {
            showDeleteAchievementDialog.value = false
            compassViewModel.deleteAchievementProgress()
        },
        action = "Ja"
    )


    // CheatCodeDialog
    if (showCheatCodeDialog.value) {
        Dialog(onDismissRequest = { showCheatCodeDialog.value = false }) {
            val cheatCodeTextField = remember { mutableStateOf("") }
            Surface(color = MaterialTheme.colors.background) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = cheatCodeTextField.value,
                        onValueChange = { cheatCodeTextField.value = it })
                    Button(onClick = { // Parse code
                        val seed = byteArrayOf(
                            -119, -128, 119, 46, 71, 63, -120, 25, -91, -44,
                            -108, 14, 13, -78, 122, -63
                        )
                        val unlockAllHash = byteArrayOf(
                            30, -16, -3, 103, 25, -110, -87, 9, -15, -53, 67, 127, 105, -36, -88,
                            79, -26, -36, -69, -42, 95, -108, 41, -1, 109, -75, 2, 82, 33, -31, 16,
                            2, 127, 0, -68, -82, -32, 79, -116, 44, -1, -5, -33, 16, 19, -54, -17,
                            -90, 25, 30, -106, 99, -49, 114, 42, 59, -97, -6, 30, -97, 22, -5, 37, 7
                        )

                        val code: ByteArray = cheatCodeTextField.value.toByteArray()
                        val md = MessageDigest.getInstance("SHA-512")
                        val hashedCode: ByteArray = md.digest(seed + code)

                        // Code for generating new hashes
                        /*
                        var arrayAsString = ""
                        for (i in hashedCode) {
                            arrayAsString = arrayAsString + i + ", "
                        }
                        Log.i("OutHash", arrayAsString)
                        */

                        if (hashedCode contentEquals unlockAllHash) {
                            compassViewModel.completeAllAchievements()
                        }
                        // More codes

                        // Close window
                        showCheatCodeDialog.value = false

                    }) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }


}

@Composable
// A button with two states. Has no onClick.
fun ToggleIconReactor(on: Boolean) {
    val toggleButtonColorOn = Primary
    val toggleButtonColorOff = if (MaterialTheme.colors.isLight) {
        CompassBackgroundDark
    } else {
        CompassBackgroundLight
    }
    Box {
        val iconId: Int
        val tint: Color
        if (on) {
            iconId = R.drawable.ic_twotone_toggle_on_24
            tint = toggleButtonColorOn
        } else {
            iconId = R.drawable.ic_twotone_toggle_off_24
            tint = toggleButtonColorOff
        }
        Icon(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            painter = painterResource(iconId),
            contentDescription = "developer mode",
            tint = tint
        )
    }
}

@Composable
fun SettingsElement(
    title: String,
    desc: String,
    iconId: Int,
    onClick: () -> Unit,
    extraInterface: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(all = 0.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = "Destination Icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(all = 16.dp)
                    .size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.80F)
                    .wrapContentWidth(Alignment.Start)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = desc,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .padding(all = 16.dp)
                    .size(40.dp)
            ) {
                extraInterface()
            }
        }
    }
}
