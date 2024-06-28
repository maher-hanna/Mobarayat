package com.maherhanna.mobarayat

data class League(val id: Int, val name: String, val games: List<Game> = emptyList())
