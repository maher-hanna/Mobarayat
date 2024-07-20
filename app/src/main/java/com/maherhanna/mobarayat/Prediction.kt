package com.maherhanna.mobarayat

import com.google.gson.annotations.SerializedName

data class
Prediction(
    @SerializedName("prediction_id")
    var id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("game_id")
    val gameId: Int,
    @SerializedName("home_team_score")
    val homeScore: Int,
    @SerializedName("home_team_score")
    val awayScore: Int
)
