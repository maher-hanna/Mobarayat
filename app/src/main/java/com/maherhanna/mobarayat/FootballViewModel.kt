package com.maherhanna.mobarayat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import okhttp3.OkHttpClient
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class FootballViewModel : ViewModel() {
    private val BASE_URL = "https://partshub.ae/mob/"

    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val client = OkHttpClient()
    private val gson = Gson()

    init {
        fetchLeagues()
        fetchUsers()
    }

    private fun fetchLeagues() {
        val request = Request.Builder()
            .url("${BASE_URL}leagues.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val leagueType = object : TypeToken<List<League>>() {}.type
                    val leagues = gson.fromJson<List<League>>(responseBody.string(), leagueType)
                    _leagues.postValue(leagues)
                }
            }
        })
    }

    private fun fetchUsers() {
        val request = Request.Builder()
            .url("${BASE_URL}users.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val userType = object : TypeToken<List<User>>() {}.type
                    val users = gson.fromJson<List<User>>(responseBody.string(), userType)
                    _users.postValue(users.map { user ->
                        user.copy(leaguePoints = user.leaguePoints ?: mutableMapOf())
                    })
                }
            }
        })
    }

    fun fetchGames(leagueId: Int, callback: (Boolean) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}games.php?league_id=$leagueId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->

                    val gameType = object : TypeToken<List<Game>>() {}.type
                    val games = gson.fromJson<List<Game>>(responseBody.string(), gameType)
                    val updatedLeagues = _leagues.value?.map { league ->
                        if (league.id == leagueId) {
                            league.copy(games = games)
                        } else {
                            league
                        }
                    }
                    _leagues.postValue(updatedLeagues)

                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            callback(true)
                        }
                    }
                } ?: viewModelScope.launch {
                    withContext(Dispatchers.Main) { callback(false) }
                }
            }
        })
    }

    fun submitPrediction(leagueId: Int, gameId: Int, prediction: Prediction) {
        val url = "${BASE_URL}submit_prediction.php?" +
                "league_id=$leagueId&game_id=$gameId&user_id=${prediction.userId}" +
                "&home_score=${prediction.homeScore}&away_score=${prediction.awayScore}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val apiResponse = gson.fromJson(responseBody.string(), ApiResponse::class.java)
                    if (response.isSuccessful && apiResponse.status == "success") {
                        addPrediction(leagueId, gameId, prediction)
                        calculatePoints(leagueId)
                    }
                }
            }
        })
    }

    private fun addPrediction(leagueId: Int, gameId: Int, prediction: Prediction) {
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
        _leagues.postValue(updatedLeagues)
    }

    fun calculatePoints(leagueId: Int) {
        val usersList = _users.value?.map { user ->
            val points = user.leaguePoints[leagueId] ?: 0
            val newPoints = points + 10 // Example point calculation
            user.leaguePoints[leagueId] = newPoints
            user
        }
        _users.postValue(usersList)
    }
}

