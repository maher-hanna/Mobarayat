package com.maherhanna.mobarayat

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue


@Composable
fun UserNameInputDialog(viewModel: FootballViewModel, context: Context, onUserConfirmed: (String,Int) -> Unit) {
    var userName by remember { mutableStateOf(TextFieldValue("")) }
    var isChecking by remember { mutableStateOf(false) }
    var userExists by remember { mutableStateOf<Boolean?>(null) }

    if (isChecking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    } else {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Enter your name") },
            text = {
                Column {
                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Name") }
                    )

                }
            },
            confirmButton = {
                Button(onClick = {
                    isChecking = true
                    val deviceId = viewModel.getDeviceId(context)
                    viewModel.checkUserExistence(userName.text, deviceId) { exists,checkedUserId ->
                        isChecking = false
                        userExists = exists
                        if (!exists) {
                            viewModel.registerUser(userName.text,deviceId){ success,userId ->
                                if(success){
                                    viewModel.userId = userId
                                    onUserConfirmed(userName.text,userId)
                                }

                            }
                        }else{
                            onUserConfirmed(userName.text,checkedUserId)

                        }
                    }
                }) {
                    Text("Confirm")
                }
            }
        )
    }
}
