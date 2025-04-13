//Experimental API usage ->
@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.lazy_susan.pages

import android.Manifest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.R
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import kotlin.math.sqrt
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.ApiHelper
import kotlin.math.*
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.tasks.await
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.Location
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.SetOptions

@Composable
fun HomeScreen(modifier: Modifier, navController: NavHostController) {
    var displayState = remember { mutableStateOf("Wheel") }
    val coroutineScope = rememberCoroutineScope()

    // Mutable states for restaurants and selected restaurant
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    val showResult = remember { mutableStateOf(false) }

    // location permissions
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var address by remember { mutableStateOf<String>("") }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                if (displayState.value == "Stats") {                Wheel(navController, displayState)

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.restaurant_stats, 10),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.distance_stats, 4),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.width(168.dp))
                            Text(text = stringResource(R.string.streak_stats, 5),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                navController.navigate(AppScreen.Stats.name)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HoneyMustardYellow),
                            modifier = Modifier
                                .width(148.dp)
                                .height(48.dp)
                                .border(1.dp, Color.Black, CircleShape)
                        ) {
                            Text(text = "Awards", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))

            // Modify this to receive list from the other one before
            Button(
                onClick = {
                    coroutineScope.launch {
                        val lat = 33.7838
                        val lng = -118.1141

                        // Fetch address from coordinates
                        address = fetchAddress(lat, lng)  // Suspend function ensures waiting for result
                        ApiHelper.cacheCoordinates(address, lat, lng) // Cache the USER coordinates for future use:

                        ApiHelper.getCachedNearbyRestaurants(lat, lng) { cachedRestaurants ->
                            Log.d("CACHE_DEBUG", "Number of cached restaurants: ${cachedRestaurants.size}")
                            if (cachedRestaurants.size < 20) {
                                // If there are less than 20 restaurants nearby USER, we check the getNearbyRestaurants
                                // 2. Fetch restaurants only after address is available
                                ApiHelper.getCoordinates(address) { addrLat, addrLng ->
                                    // Here is where it should be to check the firebase for the restaurants if they exist in the database already
                                    // Later on include the filters for the call unless changed into the
                                    ApiHelper.getNearbyRestaurants(addrLat, addrLng) { fetchedRestaurants ->
                                        if (fetchedRestaurants.isNotEmpty()) {
                                            restaurants = fetchedRestaurants

                                            //Loop through restaurants and give each one to Firebase
                                            restaurants.forEach { restaurant ->
                                                ApiHelper.getCoordinates(restaurant.address) { resLat, resLng ->
                                                    saveRestaurantToFirestore(restaurant, resLat, resLng)
                                                }
                                            }

                                            // Selected one from the many
                                            selectedRestaurant = restaurants.random()
                                            val selectedAddress = selectedRestaurant?.address ?: "No address available"

                                            ApiHelper.getCoordinates(selectedAddress) { lat2, lng2 ->
                                                val distance = calculateDistance(lat, lng, lat2, lng2)
                                                selectedRestaurant?.distance = "%.2f mi away".format(distance)
                                                // Log.d("DISTANCE_RESULT", "Distance to ${selectedRestaurant?.name}: ${"%.2f".format(distance)} mi")
                                                showResult.value = true
                                            }
                                        } else {
                                            selectedRestaurant = null
                                            showResult.value = false
                                        }
                                    }
                                }
                            }
                            else
                            {
                                // 3. If 20 or more restaurants are already cached (and within 5 miles), use those.
                                restaurants = cachedRestaurants
                                selectedRestaurant = restaurants.random()
                                showResult.value = true
                            }
                        }
                        /*
                        if (!locationPermissions.allPermissionsGranted || locationPermissions.shouldShowRationale) {
                            locationPermissions.launchMultiplePermissionRequest()
                        } else {

                            // Hardcode CSULB address in order to check here(lat and long)

                            // 1. Fetch location and address sequentially
                            val location = fusedLocationProviderClient.lastLocation.await()
                            if (location != null) {
                                val lat = location.latitude
                                val lng = location.longitude

                                // Fetch address from coordinates
                                address = fetchAddress(lat, lng)  // Suspend function ensures waiting for result

                                // 2. Fetch restaurants only after address is available
                                ApiHelper.getCoordinates(address) { addrLat, addrLng ->
                                // Here is where it should be to check the firebase for the restaurants if they exist in the database already
                                // Later on include the filters for the call unless changed into the
                                    ApiHelper.getNearbyRestaurants(addrLat, addrLng) { fetchedRestaurants ->
                                        if (fetchedRestaurants.isNotEmpty()) {
                                            restaurants = fetchedRestaurants
                                            selectedRestaurant = restaurants.random()

                                            val selectedAddress = selectedRestaurant?.address ?: "No address available"

                                            ApiHelper.getCoordinates(selectedAddress) { lat2, lng2 ->
                                                val distance = calculateDistance(lat, lng, lat2, lng2)

                                                selectedRestaurant?.let {
                                                    saveRestaurantToFirestore(it, lat2, lng2)
                                                }

                                                selectedRestaurant?.distance = "%.2f mi away".format(distance)

                                                Log.d("DISTANCE_RESULT", "Distance to ${selectedRestaurant?.name}: ${"%.2f".format(distance)} mi")
                                                showResult.value = true
                                            }
                                        } else {
                                            selectedRestaurant = null
                                            showResult.value = false
                                        }
                                    }
                                }
                            } else {
                                address = "Failed to get location"
                            }

                        }
                        */
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HoneyMustardYellow),
                modifier = Modifier
                    .width(225.dp)
                    .height(65.dp)
                    .border(3.dp, Color.Black, CircleShape)
            ) {
                Text(
                    text = stringResource(R.string.wheel_prompt),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            }
        }
    }
    if(showResult.value) {
        Result(showResult, selectedRestaurant)
    }
}

// Suspend function to fetch the location
suspend fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient): Location? {
    return try {
        fusedLocationProviderClient.lastLocation.await()
    } catch (e: Exception) {
        null
    }
}

// Suspend function to fetch address
suspend fun fetchAddress(lat: Double, lng: Double): String {
    return suspendCancellableCoroutine { continuation ->
        getAddressFromCoordinates(lat, lng) { addr ->
            continuation.resume(addr) {}
        }
    }
}

// Function to get address from coordinates
private fun getAddressFromCoordinates(lat: Double, lng: Double, callback: (String) -> Unit) {
    val API_KEY = "AIzaSyDtrWstvsa-DLgoSRDuWbQDySxjOskpRpk"
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


@Composable
fun Wheel(navController: NavHostController, displayState: MutableState<String>) {
    val painterFire = ImageBitmap.imageResource(R.drawable.fire_300)
    val painterFunnel = rememberVectorPainter(ImageVector.vectorResource(R.drawable.filter))
    Canvas(modifier = Modifier.size(360.dp)) {
        val offset = size.height * (2 - sqrt(2.0)) / 4
        drawCircle(color = PicnicTableRed)
        drawCircle(
            color = Color.Black,
            style = Stroke(width = 10f)
        )
        if (displayState.value == "Wheel") {
            drawLine(
                color = Color.Black,
                start = Offset(x = size.width / 2, y = 0f),
                end = Offset(x = size.width / 2, y = size.height),
                strokeWidth = 10f
            )
            drawLine(
                color = Color.Black,
                start = Offset(x = 0f, y = size.height / 2),
                end = Offset(x = size.width, y = size.height / 2),
                strokeWidth = 10f
            )
        }
        drawLine(
            color = Color.Black,
            start = Offset(x = offset.toFloat(), y = offset.toFloat()),
            end = Offset(
                x = size.width - offset.toFloat(),
                y = size.height - offset.toFloat()
            ),
            strokeWidth = 10f
        )
        drawLine(
            color = Color.Black,
            start = Offset(x = size.width - offset.toFloat(), y = offset.toFloat()),
            end = Offset(x = offset.toFloat(), y = size.height - offset.toFloat()),
            strokeWidth = 10f
        )
    }
    Box(modifier = Modifier
        .clip(CircleShape)
        .size(140.dp)
        .background(color = HoneyMustardYellow)
        .border(shape = CircleShape, color = Color.Black, width = 4.dp)
        .clickable {
            if (displayState.value == "Wheel") {
                displayState.value = "Stats"
            } else if (displayState.value == "Stats") {
                displayState.value = "Wheel"
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Image(painterFire, contentDescription = null)
    }
    Box {
        Canvas(modifier = Modifier.size(64.dp)) {
            translate(left = 420f, top = -504f) {
                drawCircle(color = HoneyMustardYellow)
                drawCircle(
                    color = Color.Black,
                    style = Stroke(width = 8f)
                )
            }
        }
        Canvas(modifier = Modifier.size(64.dp)) {
            translate(left = 442f, top = -478f) {
                with(painterFunnel) {
                    draw(size = Size(48.dp.toPx(), 48.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun Result(showResult: MutableState<Boolean>, restaurant: Restaurant?) {
    Dialog(onDismissRequest = { showResult.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Restaurant: ${restaurant?.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Distance: ${restaurant?.distance}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Address: ${restaurant?.address}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Phone: ${restaurant?.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Hours: ${restaurant?.hours}", style = MaterialTheme.typography.bodyMedium)

            }
        }
    }
}

fun saveRestaurantToFirestore(restaurant: Restaurant, lat: Double, lng: Double) {
    val db = Firebase.firestore
    val collectionRef = db.collection("cached_restaurants")

    // Generate a composite ID based on restaurant name and user coordinates
    // You might want to sanitize the name further if needed.
    val compositeId = "${restaurant.name.replace(" ", "_").replace("/", "-")}_${lat}_${lng}"

    // 1. Try to get an existing document by the composite ID
    collectionRef.document(compositeId).get()
        .addOnSuccessListener { document: DocumentSnapshot ->
            if (document.exists()) {
                Log.d("Firestore", "Restaurant already cached with composite ID: $compositeId")
                // Optionally, update existing data or do nothing.
            } else {
                // 2. If not found, save it to Firestore
                val restaurantData = hashMapOf(
                    "name" to restaurant.name,
                    "address" to restaurant.address,
                    "phoneNumber" to restaurant.phoneNumber,
                    "hours" to restaurant.hours,
                    "id" to compositeId, // store the composite ID
                    "latitude" to lat,
                    "longitude" to lng
                )

                collectionRef.document(compositeId).set(restaurantData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firestore", "Restaurant saved: ${restaurant.name} with ID: $compositeId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Failed to save restaurant", e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Failed to check if restaurant exists", e)
        }
}
/*
 Notes:
 - Vending Machines can count as restaurants? (Specific Vending machines like Farmers Fridge w/ full meals)
*/