package com.example.arcadecrawler

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import java.util.Random

interface ApiArcadeCrawler {
    @GET("color")
    suspend fun GetRandomColor(): RandomColor

    @GET("themePack/image")
    @Streaming//to not load full image to ram at once
    suspend fun GetLoadingScreenImage():ResponseBody

    @GET("themePack/audio")
    @Streaming
    suspend fun GetBackgroundMusic():ResponseBody

    @GET("skins")
    suspend fun GetSkins():Skins

    @GET("mushroomLayout")
    suspend fun GetMushroomLayout(): RelativeMushroomLayout

    @GET("leaderboard")
    suspend fun GetLeaderboard():List<LeaderboardGet>

    @POST("updatelead")
    suspend fun PostPlayerData(@Body player_data: LeaderboardPost): Response<JsonObject>

    @POST("powerUps")
    suspend fun PostPowerUps(@Body issuccess: PowerUpPost): List<PowerUpData>
}
val retro= Retrofit.Builder()
    .baseUrl("https://crawler-connect.vercel.app/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api=retro.create(ApiArcadeCrawler::class.java)
