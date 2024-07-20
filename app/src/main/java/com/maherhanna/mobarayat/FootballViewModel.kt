package com.maherhanna.mobarayat

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

const val MOBARAYAT_TAG = "MOBARAYAT_TAG"

class FootballViewModel : ViewModel() {
    private val BASE_URL = "https://partshub.ae/mob/"
    private val API_FOOTBALL_URL = "https://apiv3.apifootball.com/"
    private val API_KEY = "fe1b87153d955b616d80bec39f43b9326c97f28ba4b7245b16c442c46eac35e8"

    private val _leagues = MutableLiveData<List<League>>()
    val leagues: LiveData<List<League>> = _leagues

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    val cookieManager: CookieManager = CookieManager()
    private val client: OkHttpClient
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    var userId by mutableIntStateOf(-1)
    var userName by mutableStateOf("")

    private val gson = Gson()

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient
            .Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
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
                    val leagueType = object : TypeToken<List<Map<String, String>>>() {}.type
                    val leaguesData =
                        gson.fromJson<List<Map<String, String>>>(responseBody.string(), leagueType)
                    val leagues = leaguesData.map { leagueMap ->
                        val season = leagueMap["league_season"] ?: ""
                        val (seasonStart, seasonEnd) = parseSeason(season)
                        League(
                            id = leagueMap["league_id"]?.toInt() ?: 0,
                            name = leagueMap["league_name"] ?: "",
                            seasonStart = seasonStart,
                            seasonEnd = seasonEnd,
                            leagueLogo = leagueMap["league_logo"] ?: "",
                            hasPredictions = leagueMap["has_predictions"] == "true"
                        )
                    }
                    _leagues.postValue(leagues)
                }
            }
        })
    }

    private fun parseSeason(season: String): Pair<Date, Date> {
        return if (season.contains("/")) {
            val years = season.split("/")
            val startYear = years[0].toInt()
            val endYear = years[1].toInt()
            val startDate = dateFormat.parse("$startYear-01-01")!!
            val endDate = dateFormat.parse("$endYear-12-31")!!
            Pair(startDate, endDate)
        } else {
            try {
                val year = season.toInt()
                val startDate = dateFormat.parse("$year-01-01")!!
                val endDate = dateFormat.parse("$year-12-31")!!
                Pair(startDate, endDate)
            } catch (ex: Exception) {
                Pair(Date(), Date())

            }

        }
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

    fun fetchGames(league: League, callback: (Boolean) -> Unit) {
        val request = Request.Builder()
            .url(
                "${API_FOOTBALL_URL}?action=get_events&from=${dateFormat.format(league.seasonStart)}&to=${
                    dateFormat.format(
                        league.seasonEnd
                    )
                }&league_id=${league.id}&APIkey=${API_KEY}"
            )
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
                    val updatedLeagues = _leagues.value?.map { currentLeague ->
                        if (currentLeague.id == league.id) {
                            league.copy(games = games)
                        } else {
                            currentLeague
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

    fun registerUser(userName: String, deviceId: String, onSuccess: (Boolean,Int) -> Unit) {
        val url = "${BASE_URL}register_user.php?user_name=$userName&device_id=$deviceId"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onSuccess(false,-1)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val apiResponse = gson.fromJson(responseBody.string(), ApiResponse::class.java)
                    onSuccess(apiResponse.status == "success",apiResponse.user_id)
                }
            }
        })
    }

    fun checkUserExistence(userName: String, deviceId: String, callback: (Boolean,Int) -> Unit) {
        val url = "${BASE_URL}check_user.php?user_name=$userName&device_id=$deviceId"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false,-1)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val apiResponse = gson.fromJson(responseBody.string(), ApiResponse::class.java)
                    callback(apiResponse.status == "exists",apiResponse.user_id)
                }
            }
        })
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
