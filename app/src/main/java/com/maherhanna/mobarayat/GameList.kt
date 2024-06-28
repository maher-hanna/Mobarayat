package com.maherhanna.mobarayat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun GameList(league: League, onPredictionAdded: (Int, Prediction) -> Unit) {
    LazyColumn {
        items(league.games) { game ->
            var homeScore by remember { mutableStateOf("") }
            var awayScore by remember { mutableStateOf("") }

            Column {
                Text(text = "${game.homeTeam} vs ${game.awayTeam}")
                Row {
                    TextField(
                        value = homeScore,
                        onValueChange = { homeScore = it },
                        label = { Text("Home Score") }
                    )
                    TextField(
                        value = awayScore,
                        onValueChange = { awayScore = it },
                        label = { Text("Away Score") }
                    )
                }
                Button(onClick = {
                    val prediction = Prediction(1, homeScore.toInt(), awayScore.toInt()) // Example user ID
                    onPredictionAdded(game.id, prediction)
                }) {
                    Text("Submit Prediction")
                }
            }
        }
    }
}
