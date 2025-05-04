package com.example.lazy_susan.pages

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lazy_susan.ApiHelper
import com.example.lazy_susan.InfoBoxWithIcon
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.lazy_susan.R

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
    // fetch cached restaurants whenever filters or location change
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    LaunchedEffect(lat, lng, radiusMeters, minRating, cuisines) {
        ApiHelper.getCachedNearbyRestaurants(lat.toDouble(), lng.toDouble(), radiusMeters, minRating, cuisines) {
            restaurants = it
            Log.d("MAPS","Loaded ${it.size} cached restaurants")
        }
    }

    // state for which marker was tapped
    var selected by remember { mutableStateOf<Restaurant?>(null) }
    var showInfo by remember { mutableStateOf(false) }

    // map camera
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
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Restaurant name title
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, shape = CircleShape)
                                .background(Color.White, shape = CircleShape)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selected?.name ?: "Restaurant",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
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
                                text = selected?.distance ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }

                        // Info boxes
                        InfoBoxWithIcon(R.drawable.history_popup_icon, selected!!.address)
                        InfoBoxWithIcon(R.drawable.clock_icon, selected!!.hours)
                        InfoBoxWithIcon(R.drawable.phone_icon, selected!!.phoneNumber)
                    }
                }
            }
        }
}}