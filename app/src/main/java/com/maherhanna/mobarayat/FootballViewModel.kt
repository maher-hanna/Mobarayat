package com.maherhanna.mobarayat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FootballViewModel : ViewModel() {
    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    init {
        fetchLeagues()
        fetchUsers()
    }

    private fun fetchLeagues() {
        RetrofitInstance.api.getLeagues().enqueue(object : Callback<List<League>> {
            override fun onResponse(call: Call<List<League>>, response: Response<List<League>>) {
                if (response.isSuccessful) {
                    _leagues.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<League>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun fetchUsers() {
        RetrofitInstance.api.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    _users.value = response.body()?.map { user ->
                        user.copy(leaguePoints = if(user.leaguePoints == null) mutableMapOf() else user.leaguePoints )
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    fun fetchGames(leagueId: Int,onResult:(success:Boolean) -> Unit) {
        RetrofitInstance.api.getGames(leagueId).enqueue(object : Callback<List<Game>> {
            override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                if (response.isSuccessful) {
                    val updatedLeagues = _leagues.value?.map { league ->
                        if (league.id == leagueId) {
                            league.copy(games = response.body() ?: emptyList())
                        } else {
                            league
                        }
                    }
                    _leagues.value = updatedLeagues
                    onResult(true)
                }
            }

            override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                // Handle failure
                Log.d("FootballViewModel", "onFailure: ${t.message}")
                onResult(false)
            }
        })
    }

    fun submitPrediction(leagueId: Int,prediction: Prediction) {
        val predictionRequest = PredictionRequest(prediction.userId, prediction.gameId, prediction.homeScore,prediction.awayScore)
        RetrofitInstance.api.submitPrediction(predictionRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    addPrediction(leagueId,prediction)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun addPrediction(leagueId: Int,prediction: Prediction) {
        val updatedLeagues = _leagues.value?.map { league ->
            if (league.id == leagueId) {
                val updatedGames = league.games.map { game ->
                    if (game.id == prediction.gameId) {
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
        _leagues.value = updatedLeagues
    }

    fun calculatePoints(leagueId: Int) {
        val usersList = _users.value?.map { user ->
            val points = user.leaguePoints[leagueId] ?: 0
            val newPoints = points + 10 // Example point calculation
            user.leaguePoints[leagueId] = newPoints
            user
        }
        _users.value = usersList
    }
}
