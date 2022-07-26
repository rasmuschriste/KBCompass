package dk.pressere.kbkompas.achievements

import android.content.SharedPreferences
import dk.pressere.kbkompas.compass.Destination
import java.util.*

class DailyVisitAchievement(
    name: String,
    description: String,
    drawableIcon: Int,
    sharedPreferences: SharedPreferences,
    sharedPreferencesKey: String,
    destinations: Array<Destination>,
    progressForCompletion: Int,

    ) : Achievement(
    name,
    description,
    drawableIcon,
    sharedPreferences,
    sharedPreferencesKey,
    destinations,
    progressForCompletion
) {
    private var lastVisitTime = Calendar.getInstance()

    init {
        lastVisitTime.add(Calendar.YEAR, -10) // Set date far enough back that it will not count.
    }

    override fun recalculateProgress() {
        val now = Calendar.getInstance()
        if (lastVisitTime.timeInMillis > now.timeInMillis) {
            // Visit is in the future. Something went wrong.
            lastVisitTime = Calendar.getInstance()
        } else {
            if (!isSameDay(now, lastVisitTime)) {
                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DAY_OF_YEAR, -1)
                if (!isSameDay(yesterday, lastVisitTime)) {
                    // More than a day ago since last visit.
                    updateProgress(0)
                    lastVisitTime = Calendar.getInstance()
                    lastVisitTime.add(Calendar.YEAR, -10 )
                    saveState()
                }
            }
        }
    }

    override fun getProgressDataAsString(): String {
        return lastVisitTime.timeInMillis.toString()
    }

    override fun progressDataFromString(progressData: String) {
        lastVisitTime.timeInMillis = if (progressData == "") 0
        else progressData.toLong()
    }

    override fun progressReset() {
        lastVisitTime = Calendar.getInstance()
        lastVisitTime.add(Calendar.YEAR, -10)
    }

    private fun isSameDay(calendar1: Calendar, calendar2: Calendar) : Boolean {
        val c1Year = calendar1.get(Calendar.YEAR)
        val c2Year = calendar2.get(Calendar.YEAR)
        val c1Day = calendar1.get(Calendar.DAY_OF_YEAR)
        val c2Day = calendar2.get(Calendar.DAY_OF_YEAR)
        return c1Year == c2Year && c1Day == c2Day
    }

    override fun onDistanceBelow(destination: Destination) {
        val now = Calendar.getInstance()
        if (lastVisitTime.timeInMillis > now.timeInMillis) {
            // Visit is in the future. Something went wrong.
            lastVisitTime = Calendar.getInstance()
        } else {
            if (!isSameDay(now, lastVisitTime)) {
                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DAY_OF_YEAR, -1)
                if (isSameDay(yesterday, lastVisitTime)) {
                    // Was also visited yesterday
                    updateProgress(currentProgressState.value + 1)
                } else {
                    // Missed a day, start over.
                    updateProgress(1)
                }
                lastVisitTime = Calendar.getInstance()
                saveState()
            }
        }
    }
}