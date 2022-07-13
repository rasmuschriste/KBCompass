package dk.pressere.kbkompas.achievements

import androidx.compose.runtime.mutableStateOf
import dk.pressere.kbkompas.compass.Destination
import dk.pressere.kbkompas.compass.DestinationDistanceListener

private val TRIGGER_RADIUS = 50f

abstract class Achievement(
    private val progressForCompletion: Int, // The amount of progress needed to complete.
    private val progress: () -> Int  // Function that calculates the current progress.
) : DestinationDistanceListener {
    private lateinit var destinations : Array<Destination>

    private val currentProgressState = mutableStateOf(0)

    init {

    }

    fun setup(
        destinations: Array<Destination>
    ) {
        this.destinations = destinations
        // Register achievement as a listener on the destinations.
        for (d in destinations) {
            d.registerListener(this, TRIGGER_RADIUS)
        }
    }

    fun deregister() {
        for (d in destinations) {
            d.deRegisterListener(this)
        }
    }

    var isCompleted: Boolean = false

    abstract fun loadProgress()
    abstract fun saveProgress()
}