package com.maherhanna.mobarayat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FootballViewModel : ViewModel() {
    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    init {
        // Initialize with some sample data
        _leagues.value = listOf(
            League(1, "Premier League"),
            League(2, "La Liga")
        )
        _users.value = listOf(
            User(1, "Alice"),
            User(2, "Bob")
        )
    }

    fun addPrediction(leagueId: Int, gameId: Int, prediction: Prediction) {
        val updatedLeagues = _leagues.value?.map { league ->
            if (league.id == leagueId) {
                val updatedGames = league.games.map { game ->
                    if (game.id == gameId) {
                        game.copy(prediction = prediction)
                    } else {
                        game
                    }
                }
                league.copy(games = updatedGames)
            } else {
                league
            }
        }
        _leagues.value = updatedLeagues
    }

    fun calculatePoints(leagueId: Int) {
        val usersList = _users.value?.map { user ->
            val points = user.leaguePoints[leagueId] ?: 0
            // Calculate points based on predictions (example logic, adapt as needed)
            val newPoints = points + 10 // Example point calculation
            user.leaguePoints[leagueId] = newPoints
            user
        }
        _users.value = usersList
    }
}
