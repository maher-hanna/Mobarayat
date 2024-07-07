package com.maherhanna.mobarayat

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @GET("leagues.php")
    fun getLeagues(): Call<List<League>>

    @GET("games.php")
    fun getGames(@Query("league_id") leagueId: Int): Call<List<Game>>

    @GET("users.php")
    fun getUsers(): Call<List<User>>

    @POST("submit_prediction.php")
    fun submitPrediction(@Body predictionRequest: PredictionRequest): Call<ApiResponse>
}
