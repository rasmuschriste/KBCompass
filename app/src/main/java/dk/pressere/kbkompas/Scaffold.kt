package dk.pressere.kbkompas

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dk.pressere.kbkompas.achievements.AchievementMenu
import dk.pressere.kbkompas.compass.*
import dk.pressere.kbkompas.settings.SettingsContent
import dk.pressere.kbkompas.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun ScaffoldFrame(settingsViewModel: SettingsViewModel) {

    val context = LocalContext.current

    val compassViewModel: CompassViewModel =
        viewModel(factory = CompassViewModelFactory(context as Activity))
    compassViewModel.toastMessage.observe(context as LifecycleOwner) { event ->
        event.getContent()?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    // Make the compassViewModel lifecycle aware.
    // It makes little sense to have here since the scaffold is always in view,
    // but it might be useful in the future.
    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        lifeCycleOwner.lifecycle.addObserver(compassViewModel)
        onDispose { lifeCycleOwner.lifecycle.removeObserver(compassViewModel) }
    }

    val showFloatingButton = remember { mutableStateOf(true) }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Gives the current composable route shown as content.
    val currentRoute = navController
        .currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)
    // Disable the floating button when not on compass.
    showFloatingButton.value = currentRoute.value?.destination?.route == "compass"

    val closeDrawer: () -> Unit = {
        coroutineScope.launch {
            scaffoldState.drawerState.close()
        }
    }

    val openDrawer: () -> Unit = {
        coroutineScope.launch {
            scaffoldState.drawerState.open()
        }
    }

    val onFloatingButtonClick: () -> Unit = {
        //showFloatingButton.value = false
        navController.navigate(
            route = "pick_destination_compass",
        ) {
            popUpTo("compass")
            launchSingleTop = true
        }
    }

    val onErrorButtonDialogResponse: (response: Response) -> Unit = {
        when (it.errorType) {
            ErrorCode.ERROR_PERMISSION_MISSING -> {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    compassViewModel.hasPermission.value = true
                    compassViewModel.removePermissionMissingError()
                }
            }
            ErrorCode.ERROR_PROVIDER_MISSING -> {
                // Maybe not needed here since compass might receive statusUpdate from providers.
                compassViewModel.checkProviderMissingError()
            }
            else -> {
                throw Exception("No handler for ErrorCode: ${it.errorType}.")
            }
        }
    }

    Scaffold(
        topBar = { TopBar(openDrawer) },
        drawerContent = {
            DrawerContent(
                settingsViewModel,
                compassViewModel,
                navController,
                closeDrawer
            )
        },
        floatingActionButton = {
            FloatingButton(
                showFloatingButton.value,
                compassViewModel,
                onFloatingButtonClick,
                onErrorButtonDialogResponse,
                coroutineScope,
                scaffoldState.snackbarHostState
            )
        },
        scaffoldState = scaffoldState,
    ) { paddingValues ->
        NavigationMainContent(
            Modifier.padding(paddingValues),
            navController = navController,
            compassViewModel = compassViewModel,
            settingsViewModel = settingsViewModel
        )
    }

    // Check for permission and ask user if not given
    if (!compassViewModel.hasPermission.value) {
        // Might have permission anyway.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // We have permission.
            compassViewModel.hasPermission.value = true
        } else {
            // Check if we can ask for permission.
            if (shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                compassViewModel.setPermissionMissingError()
            } else {
                // Ask for the permission.
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        compassViewModel.hasPermission.value = true
                        // Remove the error since it is sometimes added from an earlier stage.
                    } else {
                        compassViewModel.setPermissionMissingError()
                    }
                }
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    // Set back button to close drawer if open.
    if (scaffoldState.drawerState.isOpen) {
        BackHandler {
            closeDrawer()
        }
    } else {
        BackHandler(false) {}
    }
}

@ExperimentalComposeUiApi
@Composable
fun NavigationMainContent(
    modifier: Modifier,
    navController: NavHostController,
    compassViewModel: CompassViewModel,
    settingsViewModel: SettingsViewModel,
) {
    NavHost(
        modifier = modifier, navController = navController, startDestination = "compass"
    ) {
        composable("compass") { CompassContent(compassViewModel) }
        composable(
            "destination_editor/{destination_index}",
            arguments = listOf(navArgument("destination_index") {
                type = androidx.navigation.NavType.Companion.IntType
            })
        ) { backStackEntry ->
            DestinationEditor(
                navController,
                compassViewModel,
                settingsViewModel,
                backStackEntry.arguments?.getInt("destination_index")
            )
        }
        composable("destinations") {
            AdvDestinationPicker(
                navController,
                compassViewModel,
                settingsViewModel
            )
        }
        composable("pick_destination_compass") {
            PickDestinationCompass(
                navController,
                compassViewModel,
                settingsViewModel
            )
        }
        composable("settings") { SettingsContent(compassViewModel, settingsViewModel) }
        composable("pick_destination_map") {
            PickDestinationShowMap(
                navController,
                compassViewModel,
                settingsViewModel
            )
        }
        composable("achievements") { AchievementMenu(compassViewModel, settingsViewModel) }
    }
}

@Composable
fun FloatingButton(
    showFloatingButton: Boolean,
    compassViewModel: CompassViewModel,
    onClick: () -> Unit,
    onErrorButtonDialogResponse: (response: Response) -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    if (showFloatingButton) {

        var colorArrow = Color.Black
        var colorDisk = Color.Black

        if (MaterialTheme.colors.isLight) {
            colorArrow = MaterialTheme.colors.primary
            colorDisk = MaterialTheme.colors.secondary
        } else {
            colorArrow = MaterialTheme.colors.onBackground
            colorDisk = MaterialTheme.colors.primaryVariant
        }

        if (compassViewModel.currentErrorCode.value == ErrorCode.ERROR_NONE) {
            FloatingActionButton(
                onClick = onClick,
                backgroundColor = colorDisk,
                modifier = Modifier.size(64.dp)
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_directions_24),
                    tint = colorArrow,
                    contentDescription = "Destinations",
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            FloatingErrorButton(
                colorDisk,
                errorCode = compassViewModel.currentErrorCode.value,
                coroutineScope,
                snackbarHostState,
                onErrorButtonDialogResponse
            )
        }
    }
}

@Composable
fun TopBar(
    onNavigationIconClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer",
                )
            }
        },
        contentColor = Color.White
    )
}

@Composable
fun DrawerContent(
    settingsViewModel: SettingsViewModel,
    compassViewModel: CompassViewModel,
    navController: NavHostController,
    closeDrawer: () -> Unit
) {
    val navigateAndClose: (String) -> Unit = {
        closeDrawer()
        navController.navigate(it) {
            popUpTo("compass")
            launchSingleTop = true
        }
    }
    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(MaterialTheme.colors.primary)
        )
        Box(modifier = Modifier.height(36.dp), contentAlignment = Alignment.BottomStart) {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .height(20.dp), contentAlignment = Alignment.BottomStart
        ) {
            /*Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )*/
        }


        DrawerElement(
            iconId = R.drawable.ic_baseline_location_searching_24,
            text = "Kompas"
        ) {
            navigateAndClose("compass")
        }

        DrawerElement(
            iconId = R.drawable.ic_baseline_map_24,
            text = "Find på kort"
        ) {
            navigateAndClose("pick_destination_map")
        }
        DrawerElement(
            iconId = R.drawable.ic_baseline_edit_location_24,
            text = "Destinationer"
        ) {
            navigateAndClose("destinations")
        }

        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

        DrawerElement(
            iconId = R.drawable.ic_baseline_cake_24,
            text = "Bedrifter"
        ) {
            navigateAndClose("achievements")
        }

        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))


        DrawerElement(
            iconId = R.drawable.ic_baseline_settings_24,
            text = "Indstillinger"
        ) {
            navigateAndClose("settings")
        }


    }
}

@Composable
fun DrawerElement(
    iconId: Int,
    text: String = "Missing",
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = text,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .size(32.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}