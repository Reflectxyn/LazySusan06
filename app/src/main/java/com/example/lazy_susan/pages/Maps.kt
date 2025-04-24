package com.example.lazy_susan.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lazy_susan.ui.theme.PicnicTableRed
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapsScreen(radiusInMiles: Int) {
    val atasehir = LatLng(34.0549, -118.2426)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(atasehir, 15f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        MapProperties(
            isMyLocationEnabled = true
        )
        Circle(
            center = atasehir,
            radius = radiusInMiles * 1609.34,
            strokeColor = PicnicTableRed,
            strokeWidth = 1.5f,
            fillColor = _root_ide_package_.androidx.compose.ui.graphics.Color(1f, 0f, 0f, 0.25f)
        )
        Marker(
            state = MarkerState(position = atasehir),
            title = radiusInMiles.toString()
        )
    }
}