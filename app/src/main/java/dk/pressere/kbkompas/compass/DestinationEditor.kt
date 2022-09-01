package dk.pressere.kbkompas.compass

import android.location.Geocoder
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dk.pressere.kbkompas.R
import dk.pressere.kbkompas.components.DefaultDialog
import dk.pressere.kbkompas.components.FloatingWindow
import dk.pressere.kbkompas.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.IOException

@ExperimentalComposeUiApi
@Composable
fun DestinationEditor(
    navController: NavHostController,
    compassViewModel: CompassViewModel,
    settingsViewModel: SettingsViewModel,
    destinationIndex: Int?
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val geocoder = Geocoder(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    var name by if (destinationIndex == -1) {
        remember { (mutableStateOf("")) }
    } else {
        remember { mutableStateOf(compassViewModel.destinations[destinationIndex!!].name) }
    }
    var address by if (destinationIndex == -1) {
        remember { (mutableStateOf("")) }
    } else {
        remember { mutableStateOf(compassViewModel.destinations[destinationIndex!!].address) }
    }
    var latitude by if (destinationIndex == -1) {
        remember { mutableStateOf("%.6f".format(0.0f)) }
    } else {
        remember { mutableStateOf("%.6f".format(compassViewModel.destinations[destinationIndex!!].location.latitude)) }
    }
    var longitude by if (destinationIndex == -1) {
        remember { mutableStateOf("%.6f".format(0.0f)) }
    } else {
        remember { mutableStateOf("%.6f".format(compassViewModel.destinations[destinationIndex!!].location.longitude)) }
    }
    var favorite by if (destinationIndex == -1) {
        remember { (mutableStateOf(true)) }
    } else {
        remember { mutableStateOf(compassViewModel.destinations[destinationIndex!!].stateOfIsFavorite.value) }
    }

    // The name of icon.
    var iconId by if (destinationIndex == -1) {
        remember { mutableStateOf("default") }
    } else {
        remember { mutableStateOf(compassViewModel.destinations[destinationIndex!!].iconId) }
    }

    var iconResourceId by remember { mutableStateOf(compassViewModel.getIconResourceByName(iconId)) }


    // If the destination can be edited.
    var enableCritical by if (destinationIndex == -1) {
        remember { mutableStateOf(true) }
    } else {
        remember { mutableStateOf(settingsViewModel.devMode.value || !compassViewModel.destinations[destinationIndex!!].preConfigured) }
    }

    var nameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var latitudeError by remember { mutableStateOf(false) }
    var longitudeError by remember { mutableStateOf(false) }

    var nameErrorText by remember { mutableStateOf("") }
    var addressErrorText by remember { mutableStateOf("") }
    var locationErrorText by remember { mutableStateOf("") }

    // Check for common errors.
    if (name == "") {
        nameError = true
        nameErrorText = "Påkrævet"
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Show dialogs.
    DefaultDialog(
        showDialog = showDeleteDialog,
        message = "Er du sikker på, at du vil slette denne destination?",
        onDismiss = { showDeleteDialog = false },
        dismiss = "Nej",
        onAction = {
            showDeleteDialog = false
            navController.popBackStack()
            compassViewModel.destinations.removeAt(destinationIndex!!)
            compassViewModel.saveAllDestinations()
            compassViewModel.updateUi(true)
        },
        action = "Ja"
    )

    var showPickIconBox by remember { mutableStateOf(false) }

    FloatingWindow(
        showPickIconBox,
        { showPickIconBox = false }
    ) {
        // TODO: Fast and dirty implementation.
        val iconsIterator = compassViewModel.getIconResourcesAndNamesIterator()


        LazyVerticalGrid (
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp)
                ) {
            for (iconPair in iconsIterator) {
                item {
                    var iconColor = MaterialTheme.colors.onBackground
                    if (iconPair.key == iconId) {
                        iconColor = MaterialTheme.colors.primary
                    }
                    OutlinedButton(
                        onClick = {
                            iconId = iconPair.key
                            iconResourceId = iconPair.value
                            showPickIconBox = false
                        },
                        enabled = true,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = iconPair.value),
                            tint = iconColor,
                            contentDescription = "icon",
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
                .fillMaxWidth()
                .height(88.dp)
        ) {
            OutlinedButton(
                onClick = { showPickIconBox = true },
                enabled = true,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResourceId),
                    contentDescription = "Pick icon",
                    modifier = Modifier.size(56.dp)
                )
            }

            Divider(Modifier.fillMaxHeight().width(1.dp))

            val favoriteIconColor = if (favorite) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onBackground
            }
            OutlinedButton(
                onClick = {
                    favorite = !favorite
                },
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(start = 8.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_favorite_24
                    ),
                    contentDescription = "Favorite",
                    tint = favoriteIconColor,
                    modifier = Modifier.size(56.dp)
                )
            }
            val enableDeleteButton = enableCritical && destinationIndex != -1
            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                enabled = enableDeleteButton,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                    contentDescription = "Delete",
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Divider()

        OutlinedTextField(
            value = name,
            onValueChange = {
                if (it.length > 20) {
                    nameError = true
                    nameErrorText = "Maksimalt 20 tegn"
                } else if (it == "") {
                    nameError = true
                    nameErrorText = "Påkrævet"
                } else {
                    nameError = false
                    nameErrorText = ""
                }
                name = it
            },
            enabled = enableCritical,
            label = { Text("Navn") },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                if (nameError) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_error_outline_24),
                        contentDescription = "Error",
                        tint = MaterialTheme.colors.primary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 2.dp)
                .fillMaxWidth()
        )

        Text(
            text = nameErrorText,
            fontSize = 12.sp,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 2.dp, bottom = 8.dp)
        )


        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            enabled = enableCritical,
            label = { Text("Addresse") },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                if (addressError) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_error_outline_24),
                        contentDescription = "Error",
                        tint = MaterialTheme.colors.primary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                .fillMaxWidth()
        )

        Text(
            text = addressErrorText,
            fontSize = 12.sp,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 2.dp, bottom = 16.dp)
        )

        Divider()

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Lokation",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .wrapContentWidth(Alignment.Start)
                    .padding(start = 12.dp, end = 8.dp, top = 16.dp, bottom = 2.dp)
            )
            // Space for a button or something.
        }

        Row (Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 2.dp),
                enabled = enableCritical,
                onClick = {
                    coroutineScope.launch {
                        // Blocking here is very likely fine.
                        @Suppress("BlockingMethodInNonBlockingContext")
                        try {
                            val locationAddresses = geocoder.getFromLocationName(address, 1)
                            if (locationAddresses.size != 0) {
                                val locationAddress = locationAddresses[0]
                                latitude = "%.6f".format(locationAddress.latitude)
                                longitude = "%.6f".format(locationAddress.longitude)
                            } else {
                                latitude = "%.6f".format(0.0f)
                                longitude = "%.6f".format(0.0f)
                            }
                        } catch (e: IOException) {
                            latitude = "%.6f".format(0.0f)
                            longitude = "%.6f".format(0.0f)
                        }
                        latitudeError = false
                        locationErrorText = ""
                    }
                }) {
                Text(text = "Brug adresse  ")
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_search_24),
                    contentDescription = "Vælg på kort"
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 2.dp),
                enabled = enableCritical,
                onClick = {
                    latitude = "%.6f".format(compassViewModel.currentLat)
                    longitude = "%.6f".format(compassViewModel.currentLong)
                    latitudeError = false
                    locationErrorText = ""
                }) {
                Text(text = "Brug placering ")
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                    contentDescription = "Vælg på kort"
                )
            }
        }

        Row(
            Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = {
                    latitude = it
                    if (parseGeoCoordinate(it)) {
                        latitudeError = false
                        locationErrorText = ""
                    } else {
                        latitudeError = true
                        locationErrorText = "Forkert format: 55.550000"
                    }
                },
                enabled = enableCritical,
                label = { Text("Latitude") },
                maxLines = 1,
                singleLine = true,
                trailingIcon = {
                    if (latitudeError) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_error_outline_24),
                            contentDescription = "Error",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                ),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .weight(1f)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = {
                    longitude = it
                    if (parseGeoCoordinate(it)) {
                        longitudeError = false
                        locationErrorText = ""
                    } else {
                        longitudeError = true
                        locationErrorText = "Forkert format: 55.550000"
                    }
                },
                enabled = enableCritical,
                label = { Text("Longitude") },
                maxLines = 1,
                singleLine = true,
                trailingIcon = {
                    if (longitudeError) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_error_outline_24),
                            contentDescription = "Error",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        Text(
            text = locationErrorText,
            fontSize = 12.sp,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 2.dp, bottom = 16.dp)
        )

        Divider()

        val saveButtonEnabled = !nameError && !addressError && !latitudeError && !longitudeError
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(text = "Fortryd")
            }
            Button(
                enabled = saveButtonEnabled,
                onClick = {
                    // Check for errors again just to make sure.
                    if (!nameError || !addressError || !latitudeError || !longitudeError) {
                        if (destinationIndex == -1) {
                            // A new destination to be added.
                            val d = Destination(
                                preConfigured = false,
                                name = name,
                                address = address,
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble(),
                                isFavorite = favorite,
                                iconId = iconId
                            )
                            compassViewModel.destinations.add(d)
                        } else {
                            val d = compassViewModel.destinations[destinationIndex!!]
                            d.name = name
                            d.address = address
                            d.location.latitude = latitude.toDouble()
                            d.location.longitude = longitude.toDouble()
                            d.stateOfIsFavorite.value = favorite
                            d.iconId = iconId
                        }
                        compassViewModel.saveAllDestinations()
                        compassViewModel.updateUi(false)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(text = "Gem")
            }
        }
    }
}

private fun parseGeoCoordinate(coordinate: String): Boolean {
    coordinate.replace(",", ".")
    val tokens = coordinate.split(".")
    if (tokens.size != 2) return false
    // Check that the tokens are numbers
    try {
        val intPart = Integer.parseInt(tokens[0])
        if (intPart < -120 || intPart > 120) return false
        Integer.parseInt(tokens[1])
        if (tokens[1].length != 6) return false
    } catch (e: Exception) {
        return false
    }
    return true
}