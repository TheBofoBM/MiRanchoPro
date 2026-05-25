package com.equipo.miranchopro.data.api

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("forecast") val forecast: Forecast
)

data class Forecast(
    @SerializedName("forecastday") val forecastday: List<ForecastDay>
)

data class ForecastDay(
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: Day
)

data class Day(
    @SerializedName("avgtemp_c") val avgTempC: Double,
    @SerializedName("condition") val condition: Condition
)

data class Condition(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String
)
