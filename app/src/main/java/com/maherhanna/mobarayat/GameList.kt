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
fun GameList(viewModel: FootballViewModel, league: League) {
    val games by remember { mutableStateOf(league.games) }

    LazyColumn {
        items(games) { game ->
            var homeScore by remember { mutableStateOf("") }
            var awayScore by remember { mutableStateOf("") }

            Column {
                Text(text = "${game.homeTeamName} vs ${game.awayTeamName}")
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
                    val prediction = Prediction(1, gameId = game.id, homeScore.toInt(), awayScore.toInt()) // Example user ID
                    viewModel.submitPrediction(leagueId = league.id,gameId = game.id,prediction)
                    viewModel.calculatePoints(league.id)
                }) {
                    Text("Submit Prediction")
                }
            }
        }
    }
}
