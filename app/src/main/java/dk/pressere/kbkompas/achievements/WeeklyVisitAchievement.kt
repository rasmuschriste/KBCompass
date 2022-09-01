package dk.pressere.kbkompas.achievements

import android.content.SharedPreferences
import dk.pressere.kbkompas.compass.Destination
import java.util.*

// Counts how many times a destination has been visited.

class WeeklyVisitAchievement(
    name: String,
    description: String,
    drawableIcon: Int,
    sharedPreferences: SharedPreferences,
    sharedPreferencesKey: String,
    destinations: Array<Destination>,
    progressForCompletion: Int,
    onCompletion : (String) -> Unit,

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
    private var lastVisitTime = Calendar.getInstance()

    init {
        // Remove some time so this date is not counted as progress.
        lastVisitTime.add(Calendar.YEAR, -10)
    }

    override fun recalculateProgress() {
        val now = Calendar.getInstance()
        if (lastVisitTime.timeInMillis > now.timeInMillis) {
            // Visit is in the future. Something went wrong.
            lastVisitTime = Calendar.getInstance()
        } else {
            if (!isSameWeek(now, lastVisitTime)) {
                val lastWeek = Calendar.getInstance()
                lastWeek.add(Calendar.DAY_OF_WEEK, -7)
                if (!isSameWeek(lastWeek, lastVisitTime)) {
                    // More than a week ago since last visit.
                    updateProgress(0)
                    lastVisitTime = Calendar.getInstance()
                    lastVisitTime.add(Calendar.YEAR, -10)
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

    private fun isSameWeek(calendar1: Calendar, calendar2: Calendar): Boolean {

        val c1 = calendar1.clone() as Calendar
        val c2 = calendar2.clone() as Calendar
        c1.firstDayOfWeek = Calendar.MONDAY
        c2.firstDayOfWeek = Calendar.MONDAY
        c1.minimalDaysInFirstWeek = 1
        c2.minimalDaysInFirstWeek = 1

        // Convert to monday.
        if (c1.get(Calendar.DAY_OF_WEEK) == 1) c1.add(Calendar.DAY_OF_WEEK, -6)
        else c1.add(Calendar.DAY_OF_WEEK, -c1.get(Calendar.DAY_OF_WEEK) + 2)

        if (c2.get(Calendar.DAY_OF_WEEK) == 1) c2.add(Calendar.DAY_OF_WEEK, -6)
        else c2.add(Calendar.DAY_OF_WEEK, -c2.get(Calendar.DAY_OF_WEEK) + 2)

        if (c1.get(Calendar.WEEK_OF_YEAR) != c2.get(Calendar.WEEK_OF_YEAR)) {
            return false
        }
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
            return false
        }
        return true
    }

    override fun onDistanceBelow(destination: Destination) {
        val now = Calendar.getInstance()
        if (lastVisitTime.timeInMillis > now.timeInMillis) {
            // Visit is in the future. Something went wrong.
            lastVisitTime = Calendar.getInstance()
        } else {
            if (!isSameWeek(now, lastVisitTime)) {
                val lastWeek = Calendar.getInstance()
                lastWeek.add(Calendar.DAY_OF_WEEK, -7)
                if (isSameWeek(lastWeek, lastVisitTime)) {
                    // Was also visited last week
                    updateProgress(currentProgressState.value + 1)
                } else {
                    // Missed a week, start over.
                    updateProgress(1)
                }
                lastVisitTime = Calendar.getInstance()
                saveState()
            }
        }
    }
}