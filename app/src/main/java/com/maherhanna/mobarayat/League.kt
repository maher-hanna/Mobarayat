package com.maherhanna.mobarayat

import java.util.Date

data class League(
    val id: Int,
    val name: String,
    val seasonStart: Date,
    val seasonEnd: Date,
    val games: List<Game> = emptyList(),
    val leagueLogo: String,
    val countryLogo: String
)
