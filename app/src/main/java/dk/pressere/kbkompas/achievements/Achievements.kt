package dk.pressere.kbkompas.achievements

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.pressere.kbkompas.settings.SettingsViewModel
import dk.pressere.kbkompas.R
import dk.pressere.kbkompas.compass.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
        LazyColumn {
            itemsIndexed(
                items = achievements
            ) { index, achievement  ->
                AchievementMenuElement(achievement = achievement) {

                }
            }
        }
    }
}

@Composable
fun AchievementMenuElement(achievement: Achievement, onClick : () -> Unit) {
    Box(
        modifier = Modifier
            .clickable {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(all = 0.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_outline_grass_24),
                contentDescription = "Destination Icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(all = 16.dp)
                    .size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.8F)
                    .wrapContentWidth(Alignment.Start)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = achievement.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = achievement.description,
                    maxLines = 4,
                    softWrap = true
                )
            }
            Text(text = achievement.currentProgressState.value.toString() + "/" + achievement.progressForCompletion)
        }
    }
}
