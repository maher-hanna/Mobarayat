package com.maherhanna.mobarayat

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun FootballApp(viewModel: FootballViewModel, paddingValues: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController,
        startDestination = "leagues",
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("leagues") {
            LeagueList(viewModel) { league ->
                navController.navigate("league/${league.id}")
            }
        }
        composable("league/{leagueId}") { backStackEntry ->
            val leagueId = backStackEntry.arguments?.getString("leagueId")?.toIntOrNull()
            val league = viewModel.leagues.value?.find { it.id == leagueId }
            league?.let {
                GameList(it) { gameId, prediction ->
                    viewModel.addPrediction(it.id, gameId, prediction)
                    viewModel.calculatePoints(it.id)
                }
            }
        }
        composable("points/{leagueId}") { backStackEntry ->
            val leagueId = backStackEntry.arguments?.getString("leagueId")?.toIntOrNull()
            leagueId?.let {
                UserPoints(viewModel, it)
            }
        }
    }
}
