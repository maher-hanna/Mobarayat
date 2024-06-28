package com.maherhanna.mobarayat

data class Game(val id: Int, val homeTeam: String, val awayTeam: String, var prediction: Prediction? = null)
