package com.example.lazy_susan.pages

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lazy_susan.ApiHelper
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapsScreen(lat: Float, lng: Float, @SuppressLint("ContextCastToActivity") filterViewModel: FilterViewModel = viewModel(LocalContext.current as ComponentActivity)) {
    val context = LocalContext.current
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // pulled your filters
    val radiusMiles = filterViewModel.selectedDistance.value.toDoubleOrNull() ?: 2.0
    val radiusMeters = radiusMiles * 1609.34
    val minRating   = filterViewModel.selectedRating.value.toDoubleOrNull() ?: 3.0
    val cuisines    = getSelectedCuisines(
        filterViewModel.selectedCuisines,
        DataSource.cuisines
    )

    /*
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
     */

    // 3) fetch cached restaurants whenever filters or location change
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    LaunchedEffect(lat, lng, radiusMeters, minRating, cuisines) {
        ApiHelper.getCachedNearbyRestaurants(lat.toDouble(), lng.toDouble(), radiusMeters, minRating, cuisines) {
            restaurants = it
            Log.d("MAPS","Loaded ${it.size} cached restaurants")
        }
    }

    // 4) state for which marker was tapped
    var selected by remember { mutableStateOf<Restaurant?>(null) }
    var showInfo by remember { mutableStateOf(false) }

    // 5) map camera
    val start = LatLng(lat.toDouble(), lng.toDouble())
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
            if (r.latitude != null && r.longitude != null) {
                val pos = LatLng(r.latitude!!, r.longitude!!)
                Marker(
                    state = MarkerState(position = pos),
                    title = r.name,
                    snippet = r.distance,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onClick = {
                        selected = r
                        showInfo = true
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
                    Text("Restaurant: ${selected!!.name}", style = MaterialTheme.typography.titleLarge)
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
}}