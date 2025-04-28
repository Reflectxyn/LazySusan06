package com.example.lazy_susan.pages

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.util.Log
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.*
import androidx.compose.runtime.getValue
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.model.Cuisine
import com.example.lazy_susan.pages.Result
import com.example.lazy_susan.pages.getSelectedCuisines
import com.example.lazy_susan.pages.getLocation
import com.example.lazy_susan.ApiHelper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import com.example.lazy_susan.Restaurant

@Composable
fun MapsScreen(filterViewModel: FilterViewModel = viewModel(LocalContext.current as ComponentActivity)) {
    val context = LocalContext.current
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // 1) grab user’s last‐known location once
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    userLat = loc.latitude
                    userLng = loc.longitude
                }
            }
        }
    }

    // 2) pull your filters
    val radiusMiles = filterViewModel.selectedDistance.value.toDoubleOrNull() ?: 2.0
    val radiusMeters = radiusMiles * 1609.34
    val minRating   = filterViewModel.selectedRating.value.toDoubleOrNull() ?: 3.0
    val cuisines    = getSelectedCuisines(
        filterViewModel.selectedCuisines,
        DataSource.cuisines
    )

    // 3) fetch cached restaurants whenever location or filters change
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    LaunchedEffect(userLat, userLng, radiusMeters, minRating, cuisines) {
        val lat = userLat ?: return@LaunchedEffect
        val lng = userLng ?: return@LaunchedEffect
        ApiHelper.getCachedNearbyRestaurants(lat, lng, radiusMeters, minRating, cuisines) {
            restaurants = it
            Log.d("MAPS","Loaded ${it.size} cached restaurants")
        }
    }

    // 4) state for which marker was tapped
    var selected by remember { mutableStateOf<Restaurant?>(null) }
    val showInfo = remember { mutableStateOf(false) }

    // 5) map camera
    val start = LatLng(userLat ?: 0.0, userLng ?: 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(start, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        MapProperties(
            isMyLocationEnabled = true
        )
        Circle(
            center = start,
            radius = radiusMeters,
            strokeColor = PicnicTableRed,
            strokeWidth = 1.5f,
            fillColor = _root_ide_package_.androidx.compose.ui.graphics.Color(10f, 0f, 0f, 0.25f)
        )
        Marker(
            state = MarkerState(position = start),
            title = "Your Location",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )
        // 7) drop a marker for each
        restaurants.forEach { r ->
            val pos = LatLng(r.latitude, r.longitude)
            Marker(
                state = MarkerState(position = pos),
                title = r.name,
                snippet = r.distance,
                onClick = {
                    selected = r
                    showInfo.value = true
                    true
                }
            )
        }
    }

    if (showInfo && selected != null) {
        Dialog(onDismissRequest = { showInfo = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Restaurant: ${selected!!.name}", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(8.dp))
                    Text("Address: ${selected!!.address}")
                    Spacer(Modifier.height(4.dp))
                    Text("Phone: ${selected!!.phoneNumber}")
                    Spacer(Modifier.height(4.dp))
                    Text("Hours: ${selected!!.hours}")
                    Spacer(Modifier.height(4.dp))
                    Text("Distance: ${selected!!.distance}")
                }
            }
        }
    }
}