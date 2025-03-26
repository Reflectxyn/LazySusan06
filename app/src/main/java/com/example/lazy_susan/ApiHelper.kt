package com.example.lazy_susan

import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Call
import okhttp3.Callback

import okhttp3.OkHttpClient
import okhttp3.Request

import okhttp3.Response
import org.json.JSONObject
import android.util.Log
import java.io.IOException

object ApiHelper {
    private const val API_KEY = "AIzaSyDtrWstvsa-DLgoSRDuWbQDySxjOskpRpk"

    fun getCoordinates(address: String, callback: (Double, Double) -> Unit) {
        val encodedAddress = address.replace(" ", "%20")
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$API_KEY"

        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to get coordinates", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val location = results.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        callback(lat, lng)
                    }
                }
            }
        })
    }

    // Function to get address from coordinates
    private fun getAddressFromCoordinates(lat: Double, lng: Double, callback: (String) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lng&key=$API_KEY"

        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to fetch address")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    val results = jsonObject.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val address = results.getJSONObject(0).getString("formatted_address")
                        callback(address)
                    } else {
                        callback("Address not found")
                    }
                }
            }
        })
    }

    fun getNearbyRestaurants(lat: Double, lng: Double, callback: (List<Restaurant>) -> Unit) {
        val url = "https://places.googleapis.com/v1/places:searchNearby"
        val payload = """
            {
              "includedTypes": ["restaurant"],
              "maxResultCount": 20,
              "locationRestriction": {
                "circle": {
                  "center": {
                    "latitude": $lat,
                    "longitude": $lng
                  },
                  "radius": 1000.0
                }
              }
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val requestBody = payload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", API_KEY)
            .addHeader("X-Goog-FieldMask", "places.displayName")
            .addHeader("X-Goog-FieldMask","places.formattedAddress")
            .addHeader("X-Goog-FieldMask","places.nationalPhoneNumber")
            .addHeader("X-Goog-FieldMask","places.regularOpeningHours.weekdayDescriptions")
            .addHeader("X-Goog-FieldMask","places.rating")

            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch restaurants", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    val placesArray = jsonObject.getJSONArray("places")
                    val restaurants = mutableListOf<Restaurant>()

                    for (i in 0 until placesArray.length()) {
                        val place = placesArray.getJSONObject(i)
                        val name = place.getJSONObject("displayName").getString("text")

                        val address = if (place.has("formattedAddress")) {
                            place.getString("formattedAddress")
                        } else {
                            "Address not available"
                        }
                        val phone = place.optString("nationalPhoneNumber", "Phone not available")
                        val hoursJsonArray = place.optJSONObject("regularOpeningHours")?.optJSONArray("weekdayDescriptions")
                        val hours = place.optJSONObject("regularOpeningHours")
                            ?.optJSONArray("weekdayDescriptions")?.let { hoursArray ->
                                val groupedHours = mutableMapOf<String, MutableList<String>>()
                                val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

                                for (i in 0 until hoursArray.length()) {
                                    val entry = hoursArray.getString(i) // Example: "Monday: 10:00 AM – 11:00 PM"
                                    val parts = entry.split(": ", limit = 2)
                                    if (parts.size == 2) {
                                        val day = parts[0] // "Monday"
                                        val time = parts[1] // "10:00 AM – 11:00 PM"

                                        groupedHours.putIfAbsent(time, mutableListOf())
                                        groupedHours[time]?.add(day)
                                    }
                                }

                                // Format the grouped hours
                                groupedHours.map { (time, groupedDays) ->
                                    if (groupedDays.size > 1) {
                                        "${groupedDays.first()}-${groupedDays.last()}: $time"
                                    } else {
                                        "${groupedDays.first()}: $time"
                                    }
                                }.joinToString("\n") // Each formatted entry on a new line
                            } ?: "Hours not available"

                        Log.d("PARSED_DATA", "Name: $name, Address: $address, Phone Number: $phone, Hours: $hours") // Print extracted data to Logcat
                        restaurants.add(Restaurant(name, address, phone, hours))
                    }
                    callback(restaurants)
                }
            }
        })
    }
}

