package com.maherhanna.mobarayat

import java.util.Date

data class League(
    val id: Int,
    val name: String,
    val seasonStart: Date,
    val seasonEnd: Date,
    val leagueLogo: String,
    val hasPredictions: Boolean,
    val games: List<Game> = emptyList(),
)
