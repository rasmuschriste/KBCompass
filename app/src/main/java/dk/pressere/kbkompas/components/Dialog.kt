package dk.pressere.kbkompas.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DefaultDialog(
    showDialog: Boolean,
    message: String,
    onDismiss: () -> Unit,
    dismiss: String,
    onAction: () -> Unit,
    action: String,
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            DialogContent(
                message = message,
                onDismiss = onDismiss,
                dismiss = dismiss,
                onAction = onAction,
                action = action
            )
        }
    }
}

@Composable
fun DialogContent(
    message: String,
    onDismiss: () -> Unit,
    dismiss: String,
    onAction: () -> Unit,
    action: String,
) {
    Surface(
        color = MaterialTheme.colors.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = message,
                modifier = Modifier.padding(top = 38.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val buttonModifier = Modifier
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                val buttonElevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)

                val buttonColors =  ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.primary
                )

                Button(
                    onClick = onDismiss,
                    modifier = buttonModifier,
                    colors = buttonColors,
                    elevation =  buttonElevation
                ) {
                    Text(text = dismiss)
                }
                Button(
                    onClick = onAction,
                    modifier = buttonModifier,
                    colors = buttonColors,
                    elevation =  buttonElevation
                ) {
                    Text(text = action)
                }
            }
        }
    }
}

// Dialog but without a dismiss button.
@Composable
fun InfoDialog(
    showDialog: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    action: String,
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            InfoDialogContent(
                message = message,
                onAction = onAction,
                action = action
            )
        }
    }
}

@Composable
fun InfoDialogContent(
    message: String,
    onAction: () -> Unit,
    action: String,
) {
    Surface(
        color = MaterialTheme.colors.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = message,
                modifier = Modifier.padding(top = 38.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val buttonModifier = Modifier
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                val buttonElevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)

                val buttonColors =  ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.primary
                )
                Button(
                    onClick = onAction,
                    modifier = buttonModifier,
                    colors = buttonColors,
                    elevation =  buttonElevation
                ) {
                    Text(text = action)
                }
            }
        }
    }
}