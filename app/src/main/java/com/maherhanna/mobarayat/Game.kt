package com.maherhanna.mobarayat

data class Game(val id: Int, val home_team: String, val away_team: String, var league_id: Int = 0, var prediction: Prediction? = null)
