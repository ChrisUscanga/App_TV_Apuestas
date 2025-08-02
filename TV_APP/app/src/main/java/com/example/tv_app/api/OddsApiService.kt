package com.example.tv_app.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OddsApiService {
    
    @GET("v4/sports")
    suspend fun getSports(
        @Query("apiKey") apiKey: String,
        @Query("all") all: Boolean = false
    ): List<Sport>
    
    @GET("v4/sports/{sport}/odds")
    suspend fun getOdds(
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String = "us",
        @Query("markets") markets: String = "h2h",
        @Query("oddsFormat") oddsFormat: String = "american"
    ): List<Event>
}

data class Sport(
    val key: String,
    val group: String,
    val title: String,
    val description: String,
    val active: Boolean,
    val has_outrights: Boolean
)

data class Event(
    val id: String,
    val sport_key: String,
    val sport_title: String,
    val commence_time: String,
    val home_team: String,
    val away_team: String,
    val bookmakers: List<Bookmaker>
)

data class Bookmaker(
    val key: String,
    val title: String,
    val last_update: String,
    val markets: List<Market>
)

data class Market(
    val key: String,
    val last_update: String,
    val outcomes: List<Outcome>
)

data class Outcome(
    val name: String,
    val price: Double
) 