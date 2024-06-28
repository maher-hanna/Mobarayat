package com.maherhanna.mobarayat

data class User(val id: Int, val name: String, val leaguePoints: MutableMap<Int, Int> = mutableMapOf())
