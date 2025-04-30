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
import com.example.lazy_susan.FirebaseDatabaseHelper
import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.tasks.await
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.Location
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.SetOptions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import com.example.lazy_susan.InfoBoxWithIcon
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.model.Cuisine
import com.google.firebase.database.FirebaseDatabase


@Composable
fun HomeScreen(
    navController: NavHostController,
    filterViewModel: FilterViewModel = viewModel(LocalContext.current as ComponentActivity)
) {
    var displayState = remember { mutableStateOf("Wheel") }
    var playingState by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Mutable states for restaurants and selected restaurant
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    val showResult = remember { mutableStateOf(false) }
    val showNoResults  = remember { mutableStateOf(false) }

    // location permissions
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var address by remember { mutableStateOf<String>("") }

    // Retrieve the selected distance from the shared FilterViewModel.
    val selectedDistance = filterViewModel.selectedDistance.value
    val selectedDistanceMiles = selectedDistance.toDoubleOrNull() ?: 2.0
    // Convert miles to meters.
    val radiusMeters = selectedDistanceMiles * 1609.34

    // Get the selected rating threshold from the ViewModel.
    val ratingThresholdStr = filterViewModel.selectedRating.value
    val minRating = ratingThresholdStr.toDoubleOrNull() ?: 3.0

    val cuisineSelection = getSelectedCuisines(
        filterViewModel.selectedCuisines,
        DataSource.cuisines
    )
    Log.d("FILTER_DEBUG", "cuisineSelection = $cuisineSelection")

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
                    if (!playingState) {
                        navController.navigate(AppScreen.Filters.name)
                    }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                navController.navigate(AppScreen.Awards.name)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HoneyMustardYellow),
                            modifier = Modifier
                                .width(148.dp)
                                .height(48.dp)
                                .border(1.5.dp, Color.Black, CircleShape)
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
                    if(displayState.value == "Wheel") {
                        playingState = !playingState
                        coroutineScope.launch {

                            delay(3000)
                            playingState = !playingState

                            // Request permissions if needed, then bail out
                            if (!locationPermissions.allPermissionsGranted) {
                                locationPermissions.launchMultiplePermissionRequest()
                                return@launch
                            }

                            // Await a real location
                            val location = getLocation(fusedLocationProviderClient)
                            if (location == null) {
                                Log.e("LOCATION", "Could not fetch location")
                                return@launch
                            }

                            val lat = location.latitude
                            val lng = location.longitude

                            address = fetchAddress(lat, lng)

                            ApiHelper.getCachedNearbyRestaurants(lat, lng, radiusMeters, minRating, cuisineSelection) { cachedRestaurants ->
                                Log.d("CACHE_DEBUG", "Number of cached restaurants: ${cachedRestaurants.size}")
                                if (cachedRestaurants.size < 20) {
                                    // If there are less than 20 restaurants nearby USER, we check the getNearbyRestaurants
                                    ApiHelper.getCoordinates(address) { addrLat, addrLng ->
                                        // Firebase existing restaurant check
                                        ApiHelper.getNearbyRestaurants(addrLat, addrLng, radiusMeters, minRating, cuisineSelection) { fetchedRestaurants ->
                                            if (fetchedRestaurants.isNotEmpty()) {
                                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                if (userId != null) {
                                                    FirebaseDatabase.getInstance().getReference("users/$userId/userRestaurants")
                                                        .get()
                                                        .addOnSuccessListener { snapshot ->
                                                            val blockedNames = snapshot.children
                                                                .filter { it.child("blocked").getValue(Boolean::class.java) == true }
                                                                .mapNotNull { it.child("name").getValue(String::class.java) }

                                                            val unblockedCached = fetchedRestaurants.filter { it.name !in blockedNames }

                                                            if (unblockedCached.isNotEmpty()) {
                                                                restaurants = unblockedCached
                                                                selectedRestaurant = unblockedCached.random()
                                                                val selectedAddress = selectedRestaurant?.address ?: "No address available"

                                                                ApiHelper.getCoordinates(selectedAddress) { lat2, lng2 ->
                                                                    if (
                                                                        selectedRestaurant?.latitude != null &&
                                                                        selectedRestaurant?.longitude != null &&
                                                                        lat != null && lng != null // user's current location
                                                                    ) {
                                                                        val distance = calculateDistance(
                                                                            lat,
                                                                            lng,
                                                                            selectedRestaurant!!.latitude!!,
                                                                            selectedRestaurant!!.longitude!!
                                                                        )
                                                                        selectedRestaurant?.distance = "%.2f mi away".format(distance)
                                                                    } else {
                                                                        selectedRestaurant?.distance = "Location unavailable"
                                                                    }
                                                                    showResult.value = true
                                                                    showNoResults.value = false

                                                                }
                                                            } else {
                                                                selectedRestaurant = null
                                                                showResult.value = false
                                                                showNoResults.value = true
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                    }}
                                else
                                {
                                    // If 20 or more restaurants are already cached (and within 5 miles), use those.
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        FirebaseDatabase.getInstance().getReference("users/$userId/userRestaurants")
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                val blockedNames = snapshot.children
                                                    .filter { it.child("blocked").getValue(Boolean::class.java) == true }
                                                    .mapNotNull { it.child("name").getValue(String::class.java) }

                                                val unblockedCached = cachedRestaurants.filter { it.name !in blockedNames }

                                                if (unblockedCached.isNotEmpty()) {
                                                    restaurants = unblockedCached
                                                    selectedRestaurant = unblockedCached.random()
                                                    val selectedAddress = selectedRestaurant?.address ?: "No address available"

                                                    ApiHelper.getCoordinates(selectedAddress) { lat2, lng2 ->
                                                        val distance = calculateDistance(lat, lng, lat2, lng2)
                                                        selectedRestaurant?.distance = "%.2f mi away".format(distance)
                                                        showResult.value = true
                                                        showNoResults.value = false
                                                    }
                                                } else {
                                                    selectedRestaurant = null
                                                    showResult.value = false
                                                    showNoResults.value = true
                                                }
                                            }
                                    }
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
    // immediately after that, added “no results” dialog:
    if (showNoResults.value) {
        NoResultsDialog(showNoResults)
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
    var accepted by remember { mutableStateOf(false) }  // Track if accepted

    Dialog(onDismissRequest = { showResult.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
                .then(Modifier.heightIn(max = 800.dp)), // limit max height
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, shape = CircleShape)
                        .background(Color.White, shape = CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = restaurant?.name ?: "Restaurant",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        fontSize = 28.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nearby:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = restaurant?.distance ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
                restaurant?.let {
                    InfoBoxWithIcon(R.drawable.history_popup_icon, it.address)
                    InfoBoxWithIcon(R.drawable.phone_icon, it.phoneNumber)
                    InfoBoxWithIcon(R.drawable.clock_icon, it.hours)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (!accepted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                FirebaseDatabaseHelper.saveRestaurantToFirebase(userId, restaurant!!)
                            }
                            accepted = true  // Hide buttons after accepting
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "Accept")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showResult.value = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(text = "Reject")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null && restaurant != null) {
                                    FirebaseDatabaseHelper.saveAndBlockRestaurant(userId, restaurant)
                                }
                                showResult.value = false},
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(text = "Block")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NoResultsDialog(showNoResults: MutableState<Boolean>) {
    Dialog(onDismissRequest = { showNoResults.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No restaurants found nearby.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try loosening your filters or increasing the search radius.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showNoResults.value = false }) {
                    Text("OK")
                }
            }
        }
    }
}
fun getSelectedRatings(checkValues: List<Boolean>, ratingStrings: List<String>): List<Double> {
    return checkValues.mapIndexedNotNull { index, isChecked ->
        if (isChecked) ratingStrings.getOrNull(index)?.toDoubleOrNull() else null
    }
}
fun getSelectedCuisines(
    selectedFlags: List<Boolean>,
    cuisines: List<Cuisine>
): List<String> {
    return cuisines
        .zip(selectedFlags)              // Pair each Cuisine with its selected-flag
        .filter { it.second }            // Keep only those pairs where flag == true
        .map { it.first.apiType }        // Extract the Cuisine.apiType from each pair
}
fun saveRestaurantToFirestore(
    restaurant: Restaurant,
    lat: Double,
    lng: Double,
    types: List<String>
) {
    val db = Firebase.firestore
    val collectionRef = db.collection("cached_restaurants")
    val compositeId = "${restaurant.name.replace(" ", "_").replace("/", "-")}_${lat}_${lng}"

    // 1. Try to get an existing document by the composite ID
    collectionRef.document(compositeId).get()
        .addOnSuccessListener { document: DocumentSnapshot ->
            if (document.exists()) {
                // Log.d("Firestore", "Restaurant already cached with composite ID: $compositeId")
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
                    "longitude" to lng,
                    "rating" to restaurant.rating,
                    // Store all types as an array field
                    "types" to types,
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