package dk.pressere.kbkompas.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.pressere.kbkompas.compass.CompassViewModel
import dk.pressere.kbkompas.components.InfoDialog
import dk.pressere.kbkompas.settings.SettingsViewModel
import dk.pressere.kbkompas.ui.theme.Gold
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AchievementMenu(compassViewModel: CompassViewModel, settingsViewModel: SettingsViewModel) {

    // Update the progress of of achievements every 10 seconds.
    // This will make sure progress can be removed if it is no longer valid.
    LaunchedEffect(true) {
        while (isActive) {
            for (a in compassViewModel.achievements) {
                if (!a.isCompleteState.value) a.recalculateProgress()
            }
            delay(10000)
        }
    }

    val showDialog = remember { mutableStateOf(!settingsViewModel.achievementDialogShown) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Bedrifter",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        val achievements = compassViewModel.achievements

        // Has to be done beforehand for some reason
        var completedCount = 0
        for (achievement in achievements) {
            if (achievement.isCompleteState.value) completedCount++
        }

        LazyColumn {
            // First draw the completed achievements.
            itemsIndexed(
                items = achievements
            ) { index, achievement ->
                if (achievement.isCompleteState.value) {
                    AchievementMenuElement(achievement = achievement) {

                    }
                }
            }

            if (completedCount != 0) {
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Then the rest
            itemsIndexed(
                items = achievements
            ) { index, achievement ->
                if (!achievement.isCompleteState.value) {
                    AchievementMenuElement(achievement = achievement) {

                    }
                }
            }
        }
    }

    // Show dialogs.
    InfoDialog(
        showDialog = showDialog.value,
        message = "Velkommen til bedrifter. KB Kompasset lader dig gennemføre bedrifter på campus ved at bruge din placering. For ikke at benytte din placering når applikationen er lukket, skal KB kompasset være åbent, før der kan laves fremskridt i bedrifter. Fremskridt bliver registeret, når der er under 50 m til den relevante destination.",
        onDismiss = { showDialog.value = false },
        onAction = {
            showDialog.value = false
            settingsViewModel.achievementDialogShown = true
        },
        action = "OK"
    )
}

@Composable
fun AchievementMenuElement(achievement: Achievement, onClick: () -> Unit) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 0.dp)
        ) {
            var progressBarColor = MaterialTheme.colors.primary
            var iconColor = MaterialTheme.colors.onBackground
            if (achievement.isCompleteState.value) {
                progressBarColor = Gold
                iconColor = Gold
            }

            Row(
                modifier = Modifier.height(96.dp)
            ) {
                // Icon
                Icon(
                    painter = painterResource(achievement.drawableIcon),
                    tint = iconColor,
                    contentDescription = "Destination Icon",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(all = 16.dp)
                        .size(40.dp)
                )
                // Middle Text
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth(0.75F)
                        .wrapContentWidth(Alignment.Start)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = achievement.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = achievement.description,
                        maxLines = 4,
                        softWrap = true
                    )
                }

                // Progress
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(text = achievement.currentProgressState.value.toString() + "/" + achievement.progressForCompletion)
                }
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(),
                progress = achievement.currentProgressState.value.toFloat() / achievement.progressForCompletion.toFloat(),
                color = progressBarColor
            )

        }
    }
}
