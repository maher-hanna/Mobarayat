package com.maherhanna.mobarayat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.maherhanna.mobarayat.ui.theme.MobarayatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobarayatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = FootballViewModel()
                    val context = LocalContext.current
                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    var userName by remember { mutableStateOf(sharedPreferences.getString("user_name", null)) }
                    viewModel.userId = sharedPreferences.getInt("user_id",-1)
                    if (userName == null) {
                        UserNameInputDialog(viewModel,this) { name,id ->
                            sharedPreferences.edit().putString("user_name", name).apply()
                            sharedPreferences.edit().putInt("user_id", id).apply()
                            viewModel.userId = id
                            viewModel.userName = name
                            userName = name
                        }
                    } else {
                        FootballApp(viewModel,innerPadding)
                    }
                }
            }
        }
    }
}
