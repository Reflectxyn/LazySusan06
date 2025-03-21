package com.example.lazy_susan.pages

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


@Composable
fun HomeScreen(modifier: Modifier, navController: NavHostController) {
    var displayState = remember { mutableStateOf("Wheel") }
    val coroutineScope = rememberCoroutineScope()

    // Mutable states for restaurants and selected restaurant
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    val showResult = remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Wheel(navController, displayState)
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
                    // Change this to the user's location later,
                    // Only checks when opened, like the map
                    coroutineScope.launch {
                        val address = "4551 Linden Ave, Long Beach, CA"

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
        Result(showResult, selectedRestaurant!!)
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
    Box {
        Canvas(modifier = Modifier
            .size(140.dp)
            .clickable {
                if (displayState.value == "Wheel") {
                    displayState.value = "Stats"
                } else if (displayState.value == "Stats") {
                    displayState.value = "Wheel"
                }
            }
        ) {
            drawCircle(color = HoneyMustardYellow)
            drawCircle(
                color = Color.Black,
                style = Stroke(width = 10f)
            )
            drawImage(painterFire, topLeft = Offset(x = 12.dp.toPx(), y = 12.dp.toPx()))
        }
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
fun Result(showResult: MutableState<Boolean>, restaurant: Restaurant) {
    Dialog(onDismissRequest = { showResult.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Restaurant: ${restaurant.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Distance: ${restaurant.distance}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Address: ${restaurant.address}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Phone: ${restaurant.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Hours: ${restaurant.hours}", style = MaterialTheme.typography.bodyMedium)

            }
        }
    }
}