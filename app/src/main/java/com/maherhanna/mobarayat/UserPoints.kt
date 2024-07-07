package com.maherhanna.mobarayat

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun UserPoints(viewModel: FootballViewModel, leagueId: Int) {
    val users by viewModel.users.observeAsState(emptyList())

    LazyColumn {
        items(users) { user ->
            val points = user.leaguePoints[leagueId] ?: 0
            Text(
                text = "${user.name}: ${points} points",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
