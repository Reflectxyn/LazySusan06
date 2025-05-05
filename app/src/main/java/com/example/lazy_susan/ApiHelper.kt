package com.example.lazy_susan

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object ApiHelper {
    private const val API_KEY = "AIzaSyDtrWstvsa-DLgoSRDuWbQDySxjOskpRpk"
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() } // Lazy initialization
    private val client = OkHttpClient()  // Reuse this instead of creating new instances

    // Function to get coordinates, with Firestore caching
    fun getCachedCoordinates(address: String, callback: (Double, Double) -> Unit) {
        checkCache(address) { cachedLocation ->
            if (cachedLocation != null) {
                Log.d("CACHE", "Using cached location: $cachedLocation")
                callback(cachedLocation.latitude, cachedLocation.longitude)
            } else {
                Log.d("CACHE", "No cache found, fetching from API")
                getCoordinates(address, callback)
            }
        }
    }

    // Function to store coordinates in Firestore
    fun cacheCoordinates(address: String, lat: Double, lng: Double) {
        val geoPoint = GeoPoint(lat, lng)
        val data = hashMapOf(
            "location" to geoPoint,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("cached_USER_coordinates").document(address)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Address cached: $address") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error caching address", e) }
    }

    // Function to check Firestore cache
    private fun checkCache(address: String, callback: (GeoPoint?) -> Unit) {
        db.collection("cached_USER_coordinates").document(address)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val cachedLocation = document.getGeoPoint("location")
                    callback(cachedLocation)
                } else {
                    callback(null) // No cache found
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking cache", e)
                callback(null)
            }
    }

    fun getCoordinates(address: String, callback: (Double, Double) -> Unit) {
        val encodedAddress = address.replace(" ", "%20")
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$API_KEY"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
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

    fun getNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusMeters: Double,
        minRating: Double,
        acceptedCuisines: List<String>,
        callback: (List<Restaurant>) -> Unit
    ) {
        // Use this to see when its called
        Log.d("DEBUG", "Function getNearbyRestaurants() called")
        Log.d("DEBUG", "Rating: $minRating")
        val url = "https://places.googleapis.com/v1/places:searchNearby"

        // Define your included and excluded types as JSON arrays (as strings)
        val includedTypes = """["restaurant"]"""
        val excludedPrimaryTypes = """["shopping_mall", "casino", "amusement_center", "movie_theater", "event_venue", "convenience_store"]"""
        val acceptedPrimaryTypesJson = if (acceptedCuisines.isNotEmpty())
            acceptedCuisines.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
        else
            "[]"

        val payload = """
            {
              "includedTypes": $includedTypes,
              "excludedPrimaryTypes": $excludedPrimaryTypes,
              "minRating": $minRating,
              "includedPrimaryTypes": $acceptedPrimaryTypesJson,
              "maxResultCount": 20,
              "locationRestriction": {
                "circle": {
                  "center": {
                    "latitude": $lat,
                    "longitude": $lng
                  },
                  "radius": $radiusMeters
                }
              }
            }
        """.trimIndent()

        //Took out media to put into parameter of toRequestBody
        val requestBody = payload.toRequestBody("application/json".toMediaType())

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
            .addHeader("X-Goog-FieldMask", "places.types")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch restaurants", e)
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string().orEmpty()
                // Log.d("API_RESPONSE", bodyStr)   // ← CHANGED: log raw JSON

                val jsonObject = JSONObject(bodyStr)

                // ← CHANGED: use optJSONArray instead of getJSONArray
                val placesArray = jsonObject.optJSONArray("places")
                if (placesArray == null) {
                    Log.w("API_ERROR", "No ‘places’ array in response – zero results")
                    callback(emptyList())         // ← CHANGED: return empty list when no places
                    return
                }

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
                    // Retrieve the rating value; default to 0.0 if missing.
                    val rating = place.optDouble("rating", 0.0)

                    val typesJson = place.optJSONArray("types")
                    val typesList = mutableListOf<String>()
                    if (typesJson != null) {
                        for (j in 0 until typesJson.length()) {
                            place.optJSONArray("types")?.getString(j)?.let { typesList += it }
                        }
                    }
                    val geom = place.optJSONObject("geometry")?.optJSONObject("location")
                    val placeLat = geom?.optDouble("lat")
                    val placeLng = geom?.optDouble("lng")



                    restaurants.add(
                        Restaurant(
                            name = name,
                            address = address,
                            phoneNumber = phone,
                            hours = hours,
                            rating = rating,
                            types = typesList,
                            latitude = placeLat,
                            longitude = placeLng
                        )
                    )

                    // Log.d("PARSED_DATA", "Name: $name,\nAddress: $address, Phone Number: $phone, Hours: $hours, Types: $typesList") // Print extracted data to Logcat
                }
                callback(restaurants)

            }
        })
    }

    fun getCachedNearbyRestaurants(
        lat: Double,
        lng: Double,
        radiusMeters: Double,
        minRating: Double,
        acceptedCuisines: List<String>,
        callback: (List<Restaurant>) -> Unit
    ) {
        Log.d("CACHE_DEBUG", "getcachedNearbyRestaurants has been called!")
        Log.d("DEBUG", "Rating: $minRating")
        // BOUNDING BOX
        val deltaLat = radiusMeters / 111000.0 // Convert meters to degrees latitude (approximation)
        val deltaLng = deltaLat / cos(Math.toRadians(lat)) // Calculate delta for longitude using the cosine of the latitude (in radians)
        val minLat = lat - deltaLat
        val maxLat = lat + deltaLat
        val minLng = lng - deltaLng
        val maxLng = lng + deltaLng

        // Define your exclusions as a list.
        val excludedTypes = listOf("shopping_mall", "casino", "amusement_center", "movie_theater", "event_venue", "convenience_store")

        db.collection("cached_restaurants")
            .whereGreaterThanOrEqualTo("latitude", minLat)
            .whereLessThanOrEqualTo("latitude", maxLat)
            .whereGreaterThanOrEqualTo("longitude", minLng)
            .whereLessThanOrEqualTo("longitude", maxLng)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("CACHE_DEBUG", "snapshot size = ${querySnapshot.size()}")

                val restaurants = mutableListOf<Restaurant>()
                for (document in querySnapshot.documents) {
                    val docLat = document.getDouble("latitude")
                    val docLng = document.getDouble("longitude")
                    if (docLat != null && docLng != null) {
                        val distance = calculateDistance(lat, lng, docLat, docLng)
                        // Compare the distance (converted to miles) with the radius
                        if (distance <= (radiusMeters / 1609.34)) {
                            val name = document.getString("name") ?: "Unknown"
                            val address = document.getString("address") ?: "Address not available"
                            val phone = document.getString("phoneNumber") ?: "Phone not available"
                            val hours = document.getString("hours") ?: "Hours not available"
                            val rating = document.getDouble("rating") ?: 0.0
                            val typesList = (document.get("types") as? List<*>)
                                ?.filterIsInstance<String>()
                                ?: emptyList()

                            // exclude unwanted primary types
                            if (typesList.any { it in excludedTypes }) continue

                            // Filter: skip if rating is below threshold
                            if (rating < minRating) continue

                            // enforce cuisine selection if any
                            if (acceptedCuisines.isNotEmpty() &&
                                typesList.none { it in acceptedCuisines }
                            ) continue

                            val distanceStr = "%.2f mi away".format(distance)
                            restaurants.add(
                                Restaurant(
                                    name = name,
                                    address = address,
                                    phoneNumber = phone,
                                    hours = hours,
                                    rating = rating,
                                    distance = distanceStr,
                                    latitude = docLat,
                                    longitude = docLng,
                                    types = typesList
                                )
                            )

                        }
                    }
                }
                callback(restaurants)
            }
            .addOnFailureListener { e ->
                Log.e("CACHE_DEBUG", "getCachedNearby failed:", e)
                callback(emptyList())
            }
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0  // Earth's radius in km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distanceKm = R * c  // Distance in km
        val distanceMiles = distanceKm * 0.621371  // Convert km to miles

        return distanceMiles
    }
}