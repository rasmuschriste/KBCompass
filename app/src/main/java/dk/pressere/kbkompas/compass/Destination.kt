package dk.pressere.kbkompas.compass

import android.location.Location
import androidx.compose.runtime.mutableStateOf

val TARGET_VERSION_CODE: Int = 3

class Destination(
    var versionCode: Int = TARGET_VERSION_CODE,
    var preConfigured: Boolean = true,
    var name: String,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    var address: String = "",
    isFavorite: Boolean = false,
    var iconId: String =  "local_drink"
) {
    var location: Location = Location("")
    val stateOfIsFavorite = mutableStateOf(false)
    val stateOfDistance = mutableStateOf(0.0f) // Last seen distance to the destination

    private var listeners : ArrayList<Pair<Float, DestinationDistanceListener>> = ArrayList()

    fun registerListener(listener: DestinationDistanceListener, distanceTrigger: Float) {
        listeners.add(Pair(distanceTrigger, listener))
    }

    fun deRegisterListener(listener: DestinationDistanceListener) {
        // Removing from the list can create problems elsewhere so we create a new one instead.
        val newListeners : ArrayList<Pair<Float, DestinationDistanceListener>> = ArrayList()
        for (l in listeners) {
            if (l.second != listener) {
                newListeners.add(l)
            }
        }
        listeners = newListeners
    }

    fun updateDistance(distance : Float) {
        stateOfDistance.value = distance
        // Notify listeners
        for (listener in listeners) {
            if (listener.first >= distance) {
                // Within trigger distance.
                listener.second.onDistanceBelow(this)
            }
        }
    }


    init {
        location.longitude = longitude
        location.latitude = latitude
        stateOfIsFavorite.value = isFavorite
    }

    fun getTokenString() : String {
        // Purge invalid charters from strings. This is not the correct way to solve but will likely
        // not cause problems as it just means that these characters are removed from names.
        val nameSafe = name.replace("|","").replace("¤","")
        val addressSafe = address.replace("|","").replace("¤","")
        return "$versionCode|$preConfigured|$nameSafe|${location.latitude}|${location.longitude}|$addressSafe|${stateOfIsFavorite.value}|$iconId"
    }

    private fun update() {
        when (versionCode) {
            TARGET_VERSION_CODE -> return
            0 -> {
                // All preconfigured destinations are favorite as of v1.
                if (preConfigured) {
                    stateOfIsFavorite.value = true
                }
                versionCode = 1
            }
            1 -> {
                // As of v2, preconfigured bars begin with a new icon.
                // A new preconfigured destination was also added but this happens in
                // CompassViewModel.
                if (preConfigured) iconId = "sports_bar"
                if (name == "KB") iconId = "local_bar"
                versionCode = 2
            }
            2 -> {
                // v 3 location "Diamanten" to its new location.
                if (name == "Diamanten") {
                    location.latitude = 55.782960
                    location.longitude = 12.521320
                }
                versionCode = 3
            }
        }
        update()
    }

    companion object {
        fun generateFromToken(s : String) : Destination{
            // IconIDMap maps iconId strings to drawable resource ints.

            val tokens = s.split("|")
            // The first token will sometimes contain " " due to whitespace when saved in xml format.
            val versionCode = tokens[0].replace(" ", "").toInt()
            val preConfigured = tokens[1].toBoolean()
            val name = tokens[2]
            val latitude = tokens[3].toDouble()
            val longitude = tokens[4].toDouble()
            val address = tokens[5]
            val isFavorite = tokens[6].toBoolean()
            val iconId = tokens[7]
            val d = Destination(
                versionCode,
                preConfigured,
                name,
                latitude,
                longitude,
                address,
                isFavorite,
                iconId
            )
            d.update()
            return d
        }
    }
}

interface DestinationDistanceListener{
    fun onDistanceBelow(destination: Destination)
}

