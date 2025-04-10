//Experimental API usage ->
@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.lazy_susan.pages

import android.Manifest
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.lazy_susan.ApiHelper
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun HomeScreen(modifier: Modifier, navController: NavHostController) {
    var displayState = remember { mutableStateOf("Wheel") }
    var playingState by remember { mutableStateOf(false) }
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

    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.padding(top = 80.dp, end = 20.dp)) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(72.dp)
                .background(color = HoneyMustardYellow)
                .border(shape = CircleShape, color = Color.Black, width = 2.dp)
                .clickable {
                    navController.navigate(AppScreen.Filters.name)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.filter),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )
        }
    }
    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                WheelAnimation(displayState, isSpinning = playingState)
                if (displayState.value == "Stats") {
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
                    playingState = !playingState
                    coroutineScope.launch {
                        //val address = "4551 Linden Ave, Long Beach, CA"
                        if (!locationPermissions.allPermissionsGranted || locationPermissions.shouldShowRationale) {
                            locationPermissions.launchMultiplePermissionRequest()
                        } else {
                            coroutineScope.launch {
                                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        val lat = it.latitude
                                        val lng = it.longitude

                                        // Fetch address from coordinates
                                        getAddressFromCoordinates(lat, lng) { addr ->
                                            address = addr
                                        }
                                    } ?: run {
                                        address = "Failed to get location"
                                    }
                                }
                            }
                        }

                        // Fetch restaurants only when button is clicked
                        ApiHelper.getCoordinates(address) { lat, lng ->
                            ApiHelper.getNearbyRestaurants(lat, lng) { fetchedRestaurants ->
                                if (fetchedRestaurants.isNotEmpty()) {
                                    restaurants = fetchedRestaurants
                                    selectedRestaurant = restaurants.random()  // Picks a random restaurant

                                    val selectedAddress = selectedRestaurant?.address ?: "No address available"

                                    //
                                    ApiHelper.getCoordinates(selectedAddress){ lat2, lng2 ->
                                        // Step 5: Calculate the distance between user and restaurant
                                        val distance = calculateDistance(lat, lng, lat2, lng2)

                                        selectedRestaurant?.distance = "%.2f mi away".format(distance)

                                        // Log the results
                                        Log.d("DISTANCE_RESULT", "Distance to ${selectedRestaurant?.name}: ${"%.2f".format(distance)} mi")

                                        // Display the result
                                        showResult.value = true

                                    }
                                } else {
                                    selectedRestaurant = null
                                    showResult.value = false
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HoneyMustardYellow),
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
fun Wheel(
    displayState: MutableState<String>,
    rotationDegrees: Float = 0f
) {
    val painterFire = ImageBitmap.imageResource(R.drawable.fire_300)
    Canvas(modifier = Modifier
        .size(360.dp)
        .rotate(rotationDegrees)
    ) {
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
            if (rotationDegrees == 0f) {
                if (displayState.value == "Wheel") {
                    displayState.value = "Stats"
                } else if (displayState.value == "Stats") {
                    displayState.value = "Wheel"
                }
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Image(painterFire, contentDescription = null)
    }
}

@Composable
fun WheelAnimation(
    displayState: MutableState<String>,
    isSpinning: Boolean = false
) {
    var currentRotation by remember { mutableStateOf(0f) }
    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(isSpinning) {
        if(isSpinning) {
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = tween(1500, easing = LinearEasing
                )
            ) {
                currentRotation = value
            }
            rotation.animateTo(
                targetValue = currentRotation + 50,
                animationSpec = tween(
                    durationMillis = 1250,
                    easing = LinearOutSlowInEasing
                )
            ) {
                currentRotation = value
            }
            rotation.snapTo(0f)
        }
    }
    Wheel(
        displayState = displayState,
        rotationDegrees = rotation.value
    )
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