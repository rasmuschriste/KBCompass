package dk.pressere.kbkompas.achievements

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import dk.pressere.kbkompas.compass.Destination
import dk.pressere.kbkompas.compass.DestinationDistanceListener
import kotlin.math.min

private val TRIGGER_RADIUS = 50f


abstract class Achievement(
    val name: String,
    val description: String,
    val drawableIcon: Int,
    val sharedPreferences: SharedPreferences,
    val sharedPreferencesKey: String,
    protected val destinations: Array<Destination>,
    val progressForCompletion: Int
) : DestinationDistanceListener {
    val currentProgressState = mutableStateOf(0)
    val isCompleteState = mutableStateOf(false)

    private var isListening : Boolean = false


    fun updateProgress(progress : Int) {
        // Cannot be updated once completed.
        if (!isCompleteState.value) {
            currentProgressState.value = min(progress, progressForCompletion)
            if (currentProgressState.value == progressForCompletion) {
                isCompleteState.value = true
                deregister()
            }
        }
    }

    // Can maybe be part of init but this gives a warning.
    // Upon completion. The object will call deregister to remove listeners.
    fun setup() {
        loadState()
        // Register achievement as a listener on the destinations.
        // Completed achievements do not need to listen on destinations.
        if (!isCompleteState.value) {
            for (d in destinations) {
                d.registerListener(this, TRIGGER_RADIUS)
            }
            isListening = true
        }
    }

    private fun deregister() {
        if (isListening) {
            for (d in destinations) {
                d.deRegisterListener(this)
            }
        }
        isListening = false
    }

    private fun loadState() {
        currentProgressState.value = sharedPreferences.getInt(sharedPreferencesKey + "_p", 0)
        if (currentProgressState.value == progressForCompletion) {
            isCompleteState.value = true
        }
        val progressData = sharedPreferences.getString(sharedPreferencesKey, "")
        progressDataFromString(progressData!!)
    }

    fun saveState() {
        // Subclasses must call this themselves when appropriate.
        val progressData = getProgressDataAsString()
        sharedPreferences.edit().putString(sharedPreferencesKey, progressData)
            .putInt(sharedPreferencesKey + "_p", currentProgressState.value)
            .apply()
    }

    fun clearProgress() {
        progressReset()
        if (isCompleteState.value) {
            isCompleteState.value = false
            currentProgressState.value = 0
            // Setup listeners again
            for (d in destinations) {
                d.registerListener(this, TRIGGER_RADIUS)
            }
            isListening = true
        } else {
            updateProgress(0)
        }
        saveState()
    }

    // Recalculate the progress in case it has changed (decreased) since the last destination tick.
    // This can be called at any time to update the the progress displayed to the ui.
    // Calling this on a completed achievement will have no affect.
    abstract fun recalculateProgress()

    abstract fun getProgressDataAsString() : String
    abstract fun progressDataFromString(progressData : String)
    abstract fun progressReset() // The object does no have to set progress = 0, only reset private fields.

}