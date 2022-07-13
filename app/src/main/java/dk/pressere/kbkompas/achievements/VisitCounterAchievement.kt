package dk.pressere.kbkompas.achievements

import dk.pressere.kbkompas.compass.Destination

class VisitCounterAchievement(
    var destinationName : String,
    var countToReach : Int, progressForCompletion: Int, progress: () -> Int
) : Achievement(progressForCompletion, progress) {
    // An achievement that counts the number of visits to a destination.

    var progress = 0
    override fun onDistanceBelow(destination: Destination) {
        TODO("Not yet implemented")
    }

    override fun loadProgress() {
        TODO("Not yet implemented")
    }

    override fun saveProgress() {
        TODO("Not yet implemented")
    }
}