package com.maherhanna.mobarayat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LeagueList(viewModel: FootballViewModel, onLeagueSelected: (League) -> Unit) {
    val leagues by viewModel.leagues.observeAsState(emptyList())

    LazyColumn {
        items(leagues.sortedByDescending { it.hasPredictions }) { league ->
            Text(
                text = league.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = if (league.hasPredictions) Color(223, 255, 224, 255) else MaterialTheme.colorScheme.background)
                    .clickable {
                        onLeagueSelected(league)
                    }
                    .padding(16.dp)
            )
        }
    }
}
