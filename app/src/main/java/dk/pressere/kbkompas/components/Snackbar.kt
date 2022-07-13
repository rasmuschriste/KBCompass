package dk.pressere.kbkompas.components

import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Example use
//launchDefaultSnackbar("text", "Close", coroutineScope = coroutineScope, snackbarHostState,
//            onDismiss = { Log.d("test", "dismiss") },
//            onAction = { Log.d("test", "perform") }
//        )

fun launchDefaultSnackbar(
    message: String,
    actionLabel: String,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit = {},
    onAction: () -> Unit = {},
) {
    coroutineScope.launch {
        when (snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel
        )) {
            SnackbarResult.Dismissed -> onDismiss()
            SnackbarResult.ActionPerformed -> onAction()
        }
    }
}