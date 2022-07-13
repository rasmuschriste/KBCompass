package dk.pressere.kbkompas

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dk.pressere.kbkompas.components.DefaultDialog
import dk.pressere.kbkompas.components.launchDefaultSnackbar
import kotlinx.coroutines.CoroutineScope

enum class ErrorCode {
    ERROR_NONE, ERROR_PERMISSION_MISSING, ERROR_LOCATION_MISSING, ERROR_PROVIDER_MISSING
}

class Response(val errorType : ErrorCode, val result: ActivityResult)

@Composable
fun FloatingErrorButton(
    backgroundColor : Color,
    errorCode: ErrorCode,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onResponse: (result: Response) -> Unit
) {

    var onClick = {}
    var tint = Color.Black

    val resolveErrorCode = remember { mutableStateOf(ErrorCode.ERROR_NONE) }

    when (errorCode) {
        ErrorCode.ERROR_NONE -> {
            // Should not be visible so do nothing.
        }
        ErrorCode.ERROR_PERMISSION_MISSING -> {
            onClick = { resolveErrorCode.value = ErrorCode.ERROR_PERMISSION_MISSING }
            tint = Color.Red
        }
        ErrorCode.ERROR_LOCATION_MISSING -> {
            onClick = { resolveErrorCode.value = ErrorCode.ERROR_LOCATION_MISSING }
            tint = Color.Blue
        }
        ErrorCode.ERROR_PROVIDER_MISSING -> {
            onClick = { resolveErrorCode.value = ErrorCode.ERROR_PROVIDER_MISSING }
            tint = Color.Red
        }
    }

    val context = LocalContext.current

    val permissionResolveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        onResponse(Response(ErrorCode.ERROR_PERMISSION_MISSING, it))
    }

    val providerResolveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        onResponse(Response(ErrorCode.ERROR_PROVIDER_MISSING, it))
    }

    when (resolveErrorCode.value) {
        ErrorCode.ERROR_NONE -> {
            // Should not be visible so do nothing.
        }
        ErrorCode.ERROR_PERMISSION_MISSING -> {
            val showDialog = remember { mutableStateOf(true) }
            DefaultDialog(
                showDialog = showDialog.value,
                message = stringResource(R.string.dialog_request_location_permission_text),
                dismiss = stringResource(id = R.string.close),
                action = stringResource(id = R.string._continue),
                onDismiss = {
                    showDialog.value = false
                    resolveErrorCode.value = ErrorCode.ERROR_NONE
                },
                onAction = {
                    showDialog.value = false
                    resolveErrorCode.value = ErrorCode.ERROR_NONE
                    // launch intent
                    val appSettingsIntent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                    appSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
                    permissionResolveLauncher.launch(appSettingsIntent)
                }
            )
        }
        ErrorCode.ERROR_LOCATION_MISSING -> {
            launchDefaultSnackbar(
                message = stringResource(id = R.string.finding_locaiton),
                actionLabel = stringResource(id = R.string.close),
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
            resolveErrorCode.value = ErrorCode.ERROR_NONE
        }

        ErrorCode.ERROR_PROVIDER_MISSING -> {
            val showDialog = remember { mutableStateOf(true) }
            DefaultDialog(
                showDialog = showDialog.value,
                message = stringResource(R.string.dialog_request_provider_enable_text),
                dismiss = stringResource(id = R.string.close),
                action = stringResource(id = R.string._continue),
                onDismiss = {
                    showDialog.value = false
                    resolveErrorCode.value = ErrorCode.ERROR_NONE
                },
                onAction = {
                    showDialog.value = false
                    resolveErrorCode.value = ErrorCode.ERROR_NONE
                    // launch intent
                    val locationSettingsIntent = Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                    )
                    locationSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
                    providerResolveLauncher.launch(locationSettingsIntent)
                }
            )
        }
    }

    val targetAlpha = remember { mutableStateOf(0.0f) }
    val alpha: Float by animateFloatAsState(
        targetValue = targetAlpha.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    FloatingActionButton(
        onClick = onClick,
        backgroundColor = backgroundColor,
        modifier = Modifier.size(64.dp)
    )
    {
        val fadedTint = Color(tint.red, tint.green, tint.blue, alpha)
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_error_outline_24),
            tint = fadedTint,
            contentDescription = "Error",
            modifier = Modifier.size(32.dp)
        )
    }
    targetAlpha.value = 1.0f
}