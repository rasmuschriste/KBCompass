package dk.pressere.kbkompas.achievements

import android.content.SharedPreferences
import dk.pressere.kbkompas.compass.Destination
import java.util.*

// A visit to one of the destinations counts towards the progress.
// Each destination can only contribute with one progress.
// If the destination was visited more than destinationTimeoutMillis milliseconds ago,
// it will not be counted.
class CollectionVisitAchievement(
    name: String,
    description: String,
    drawableIcon : Int,
    sharedPreferences: SharedPreferences,
    sharedPreferencesKey: String,
    destinations: Array<Destination>,
    progressForCompletion: Int,
    onCompletion : (String) -> Unit,
    private val destinationTimeoutMillis : Long
) : Achievement(
    name,
    description,
    drawableIcon,
    sharedPreferences,
    sharedPreferencesKey,
    destinations,
    progressForCompletion,
    onCompletion
) {
    private val timeStamps : LongArray = LongArray(destinations.size) {-1}

    override fun recalculateProgress() {
        val currentTime = Calendar.getInstance().timeInMillis
        var progress = 0
        for (t in timeStamps) {
            if (t != -1L && currentTime - t < destinationTimeoutMillis) {
                ++progress
            }
        }
        updateProgress(progress)
        saveState()
    }

    override fun getProgressDataAsString(): String {
        val s = StringBuilder(timeStamps[0].toString())
        for (t in 1 until timeStamps.size) {
            s.append(" ").append(timeStamps[t].toString())
        }
        return s.toString()
    }

    override fun progressDataFromString(progressData: String) {
        if (progressData == "" ) {
            for (t in timeStamps.indices) {
                timeStamps[t] = -1
            }
        } else {
            val elements = progressData.split(' ')
            for (t in timeStamps.indices) {
                timeStamps[t] = elements[t].toLong()
            }
        }
    }

    override fun progressReset() {
        for (i in timeStamps.indices) {
            timeStamps[i] = -1
        }
    }


    override fun onDistanceBelow(destination: Destination) {
        // Find the destination in the array
        for (i in destinations.indices) {
            if (destinations[i] == destination) {
                // Update the timeStamp
                timeStamps[i] = Calendar.getInstance().timeInMillis
                // We need to check every location again since some might have timed out.
                recalculateProgress()
                break
            }
        }
    }
}