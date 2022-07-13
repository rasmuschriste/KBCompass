package dk.pressere.kbkompas.compass

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import dk.pressere.kbkompas.ErrorCode
import dk.pressere.kbkompas.R
import dk.pressere.kbkompas.bearing_provider.BearingProvider
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.collections.sortByDescending

// Activity is needed here for the location system. Might remove later.
class CompassViewModel(val activity: Activity) : ViewModel(), BearingProvider.Callback, LifecycleEventObserver {
    private var bearingOffset = 0f
    private var bearingCurrent = 0f

    private val nullDestination = Destination(name = "")

    val targetRotation = mutableStateOf(0F)
    val nameDest = mutableStateOf(activity.getString(R.string.text_missing))
    val distDest = mutableStateOf(0.0F)
    
    private var currentDestination = nullDestination
    private var lastDestination = nullDestination
    

    private val debugText = ""

    // Maps strings to drawable resource ids
    private val iconIdMap = HashMap<String, Int>()

    var bearingProvider: BearingProvider = BearingProvider(activity, this)
    var destinations = mutableStateListOf<Destination>()
    //val stateOfDestinations: MutableState<ArrayList<Destination>> = mutableStateOf(ArrayList())

    val hasPermission = mutableStateOf(false)

    private val errorHeap : TreeSet<Error> = TreeSet()
    val currentErrorCode = mutableStateOf(ErrorCode.ERROR_NONE)
    // The error with higher priority will be shown first.
    private val errorPermissionMissing = Error(10, ErrorCode.ERROR_PERMISSION_MISSING)
    private val errorProviderMissing = Error(9, ErrorCode.ERROR_PROVIDER_MISSING)
    private val errorLocationMissing = Error(1, ErrorCode.ERROR_LOCATION_MISSING)

    init {
        // Setup hardcoded icons.
        iconIdMap["default"] = R.drawable.ic_outline_celebration_24
        iconIdMap["local_drink"] = R.drawable.ic_outline_local_drink_24
        iconIdMap["pedal_bike"] = R.drawable.ic_baseline_pedal_bike_24
        iconIdMap["bus"] = R.drawable.ic_outline_directions_bus_24
        iconIdMap["car"] = R.drawable.ic_outline_directions_car_24
        iconIdMap["home"] = R.drawable.ic_outline_home_24
        iconIdMap["local_bar"] = R.drawable.ic_outline_local_bar_24
        iconIdMap["local_hotel"] = R.drawable.ic_outline_local_hotel_24
        iconIdMap["pool"] = R.drawable.ic_outline_pool_24
        iconIdMap["city"] = R.drawable.ic_outline_location_city_24
        iconIdMap["sports_bar"] = R.drawable.ic_outline_sports_bar_24
        iconIdMap["baseball"] = R.drawable.ic_outline_sports_baseball_24
        iconIdMap["wine_bar"] = R.drawable.ic_outline_wine_bar_24
        iconIdMap["grass"] = R.drawable.ic_outline_grass_24
        iconIdMap["icecream"] = R.drawable.ic_outline_icecream_24


        // Read configured. Else generate from preconfigured string.
        val sharedPreferences = activity.getPreferences(ComponentActivity.MODE_PRIVATE)
        var configuredDestinations = sharedPreferences.getString("destinations", "")
        if (configuredDestinations == "" || configuredDestinations == null) {
            // No destinations found.
            configuredDestinations = activity.getString(R.string.all_destinations_preference)
            sharedPreferences.edit().putString("destinations", configuredDestinations).apply()
        } else {
            // For a special case update, we must check if destinations are version 1.
            // Doing it this way is a bit hacky but should work as "KB" is always the first.
            if (configuredDestinations[0] == '1') {
                // Add a new destination to the list.
                configuredDestinations += "¤1|true|Verners|55.730752|12.400113|Lautrupvang 19, 2750 Ballerup|true|local_drink¤"
            }
        }
        parseDestinations(configuredDestinations)
        // Save the destinations in case they are updated to a newer version.
        saveAllDestinations()

        setTargetDestination(destinations[0])
        lastDestination = destinations[0]
    }

    private fun parseDestinations(d : String) {
        val destinationTokens = d.split("¤")
        for (destinationToken in destinationTokens) {
            if (destinationToken != "") {
                val destination = Destination.generateFromToken(destinationToken)
                destinations.add(destination)
            }
        }
    }

    fun setTargetDestination(destination: Destination) {
        bearingProvider.setTargetLocation(destination.location)

        // Update ui.
        if (destination.name == "") {
            // Null destination -> Should not show anything in field.
            nameDest.value = ""
            distDest.value = -1.0f
        } else {
            nameDest.value = destination.name
            distDest.value = bearingProvider.distance
        }
        currentDestination = destination
    }

    fun getIconResourceByName(iconName : String) : Int {
        return iconIdMap[iconName]!!
    }

    fun getIconResourcesAndNamesIterator() : MutableIterator<MutableMap.MutableEntry<String, Int>>{
        return iconIdMap.entries.iterator()
    }

    
    private fun registerError(error : Error) {
        errorHeap.add(error)
        if (errorHeap.size == 1 && currentDestination.name != "") {
            lastDestination = currentDestination
            setTargetDestination(nullDestination)
        }
    }
    
    private fun unregisterError(error : Error) {
        // Error should be the same instance as the one added.
        errorHeap.remove(error)
        if (errorHeap.isEmpty()) {
            setTargetDestination(lastDestination)
        }
    }

    fun distanceToDestination(destination: Destination): Float {
        return bearingProvider.getDistance(destination.location)
    }

    fun updateUi(resetTarget : Boolean) {
        if (resetTarget && currentDestination.name != "") {
            setTargetDestination(destinations[0])
        } else {
            setTargetDestination(currentDestination)
        }
    }

    fun saveAllDestinations() {
        val s = StringBuilder("")
        if (destinations.size > 0) {
            s.append(destinations[0].getTokenString())
        }
        for (i in 1 until destinations.size) {
            s.append("¤").append(destinations[i].getTokenString())
        }
        val sharedPreferences = activity.getPreferences(ComponentActivity.MODE_PRIVATE)
        sharedPreferences.edit().putString("destinations",s.toString()).apply()
    }

    fun purgePreferences() {
        val sharedPreferences = activity.getPreferences(ComponentActivity.MODE_PRIVATE)
        sharedPreferences.edit().putString("destinations","").apply()
    }

    fun setPermissionMissingError() {
        registerError(errorPermissionMissing)
        updateErrorCode()
    }

    fun removePermissionMissingError() {
        unregisterError(errorPermissionMissing)
        updateErrorCode()
    }

    fun checkProviderMissingError() {
        if (!bearingProvider.isGPSProviderEnabled) {
            registerError(errorProviderMissing)
        } else {
            unregisterError(errorProviderMissing)
        }
        updateErrorCode()
    }

    private class Error(
        private val priority : Int = -1,
        val errorCode : ErrorCode
    ) : Comparable<Error>{
        override fun compareTo(other: Error): Int {
            return (-priority).compareTo(-other.priority)
        }
    }

    override fun onBearingChanged(bearing: Float) {
        if (currentDestination.name == "") {
            targetRotation.value = 0.0f
            bearingOffset = 0.0f
            bearingCurrent = 0.0f
        } else {
            // Translate [0-360] to continues.
            if (bearingCurrent > 180 && bearing < 180) {
                if (bearingCurrent - bearing > 180) {
                    bearingOffset += 360f
                }
            } else if (bearingCurrent < 180 && bearing > 180) {
                if (bearingCurrent - bearing < -180) {
                    bearingOffset -= 360f
                }
            }
            bearingCurrent = bearing

            targetRotation.value = bearingOffset + bearing
        }
    }

    override fun statusUpdate(value: Int) {
        when (value) {
            BearingProvider.LOCATION_MISSING -> registerError(errorLocationMissing)
            BearingProvider.LOCATION_FOUND -> unregisterError(errorLocationMissing)
            BearingProvider.LOCATION_GPS_PROVIDER_DISABLED -> registerError(errorProviderMissing)
            BearingProvider.LOCATION_GPS_PROVIDER_ENABLED -> unregisterError(errorProviderMissing)
        }
        updateErrorCode()
    }

    private fun updateErrorCode() {
        // Updates the current errorCode thus also updates the view.
        if (errorHeap.isEmpty()) {
            currentErrorCode.value = ErrorCode.ERROR_NONE
        } else {
            currentErrorCode.value = errorHeap.first().errorCode
        }
    }

    override fun distanceChanged(distance: Float) {
        distDest.value = distance
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> bearingProvider.disable()
            Lifecycle.Event.ON_RESUME -> {
                bearingProvider.enable()
                if (!bearingProvider.isGPSProviderEnabled) {
                    registerError(errorProviderMissing)
                    updateErrorCode()
                }
            }
            else -> {}
        }
    }

}