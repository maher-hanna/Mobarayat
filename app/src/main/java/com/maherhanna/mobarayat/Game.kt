package com.maherhanna.mobarayat

data class Game(val id: Int, val match_hometeam_name: String, val match_awayteam_name: String, var league_id: Int = 0, var prediction: Prediction? = null)
