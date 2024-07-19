package com.maherhanna.mobarayat

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("match_id")
    val id: Int,
    @SerializedName("match_hometeam_name")
    val homeTeamName: String,
    @SerializedName("match_awayteam_name")
    val awayTeamName: String,
    @SerializedName("league_id")
    var leagueId: Int = 0,
    var prediction: Prediction? = null
)
