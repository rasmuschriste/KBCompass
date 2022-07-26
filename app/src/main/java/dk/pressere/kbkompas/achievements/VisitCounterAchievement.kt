package dk.pressere.kbkompas.achievements

import android.content.SharedPreferences
import dk.pressere.kbkompas.compass.Destination
import java.util.*

// A visit to any of the given destinations count 1 towards the progress.
// A visit is only counted if timeBetweenVisitsMillis milliseconds has passed since the last visit.
class VisitCounterAchievement(
    name: String,
    description: String,
    drawableIcon : Int,
    sharedPreferences: SharedPreferences,
    sharedPreferencesKey: String,
    destinations: Array<Destination>,
    progressForCompletion: Int,
    private val timeBetweenVisitsMillis: Long
) : Achievement(
    name,
    description,
    drawableIcon,
    sharedPreferences,
    sharedPreferencesKey,
    destinations,
    progressForCompletion
) {

    private var lastVisit : Long = 0

    // An achievement that counts the number of visits to a destination.
    override fun onDistanceBelow(destination: Destination) {
        val time = Calendar.getInstance().timeInMillis
        //Log.i("TEST", lastVisit.toString() + "   " + time.toString())
        if ((time - lastVisit) > timeBetweenVisitsMillis) {
            lastVisit = time
            updateProgress(currentProgressState.value + 1)
            saveState()
        }
    }

    override fun recalculateProgress() {
        // Progress can never decrease so we don't have to do anything.
    }

    override fun getProgressDataAsString(): String {
        //Log.i("TEST", lastVisit.toString())
        return lastVisit.toString()
    }

    override fun progressDataFromString(progressData: String) {
        lastVisit = if (progressData == "") 0
        else progressData.toLong()
    }

    override fun progressReset() {
        lastVisit = 0
    }

}