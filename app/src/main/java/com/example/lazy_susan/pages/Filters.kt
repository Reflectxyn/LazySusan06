package com.example.lazy_susan.pages

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.Filters
import com.example.lazy_susan.PresetViewModel
import com.example.lazy_susan.PresetViewModelFactory
import com.example.lazy_susan.R
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.ui.theme.LightGray
import com.example.lazy_susan.ui.theme.Typography
import com.google.android.gms.location.LocationServices


val cuisineLabels = listOf("Italian", "Japanese", "Thai", "Mexican", "Indian", "Chinese", "Greek", "American")
val ratingLabels = listOf("2", "3", "4", "5")
val distanceOptions = listOf("1", "2", "5", "10", "15")


@Composable
fun FiltersScreen(
    navController: NavController,
    filterVm: FilterViewModel = viewModel(LocalContext.current as ComponentActivity),
    userId: String,
    presetId: String = "",
    presetViewModel: PresetViewModel = viewModel(factory = PresetViewModelFactory(userId))
) {
    /*
    val cuisineSelections = filterViewModel.selectedCuisineBooleans
    var distanceDefault = filterViewModel.selectedDistance
     */

    val preset = presetViewModel
        .presets
        .observeAsState(emptyList())
        .value
        .find { it.id == presetId }

    val cuisineList     = filterVm.selectedCuisines      // Boolean List
    val ratingDefault   = filterVm.selectedRating        // String
    val distanceDefault = filterVm.selectedDistance      // String

    LaunchedEffect(preset) {
        preset?.let {
            it.filters.cuisines.forEachIndexed { index, value -> cuisineList[index] = value }
            ratingDefault.value = it.filters.rating.toString()
            distanceDefault.value = it.filters.distance.toString()
        }
    }
    // ← CHANGED: fetch the user’s last‐known location ONCE
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    userLat = it.latitude
                    userLng = it.longitude
                }
            }
        }
    }
    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(brush = SolidColor(Color.White), alpha = 0.8f)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .width(348.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(174.dp))
                            .background(color = Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Filter",
                            style = Typography.headlineLarge
                        )
                    }
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cuisine",
                            style = Typography.headlineSmall
                        )
                    }
                }
            }
            // CuisineFilter(cuisineList)
            itemsIndexed(cuisineLabels) { index, cuisine ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 44.dp)
                ) {
                    Checkbox(
                        checked = cuisineList[index],
                        onCheckedChange = { checked ->
                            cuisineList[index] = checked
                            Log.d("FILTER_DEBUG", "toggled $cuisine → $checked")
                        }
                    )
                    Text(text = cuisine, modifier = Modifier.width(100.dp))
                }
            }
            //Ratings
            item(span = { GridItemSpan(2) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ratings",
                            style = Typography.headlineSmall
                        )
                    }
                    //Distance
                    RatingFilter(ratingDefault)
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Distance (Miles)",
                            style = Typography.headlineSmall
                        )
                    }
                    DistanceFilter(distanceDefault)
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color.White)
                            .border( width = 2.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                            .clickable {
                                // only navigate once we have a real location:
                                val lat = userLat
                                val lng = userLng
                                if (lat != null && lng != null) {
                                    navController.navigate("${AppScreen.Maps.name}/$lat/$lng")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Map",
                            style = Typography.titleLarge
                        )
                    }
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color = Color.White)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    cuisineList.forEachIndexed { index, _ ->
                                        cuisineList[index] = false
                                    }
                                    ratingDefault.value = "3"
                                    distanceDefault.value = "2"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Clear",
                                style = Typography.titleLarge
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color = Color.White)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    val cuisines = cuisineList.toList()
                                    val rating = ratingDefault.value.toIntOrNull() ?: 3
                                    val distance = distanceDefault.value.toIntOrNull() ?: 2

                                    if (preset != null) {
                                        // Update existing preset
                                        val updated =
                                            preset.copy(
                                                filters = Filters(
                                                    cuisines,
                                                    rating,
                                                    distance
                                                )
                                            )
                                        presetViewModel.updatePreset(updated)
                                    } else {
                                        // Add new preset
                                        presetViewModel.addPreset(cuisines, rating, distance)
                                    }

                                    navController.popBackStack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save",
                                style = Typography.titleLarge
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color = Color.White)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    navController.navigateUp()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                style = Typography.titleLarge
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier
                            .width(356.dp)
                            .padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CuisineFilter(checkValue: MutableList<Boolean>) {
    Column {
        DataSource.cuisines.forEachIndexed { index, cuisine ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checkValue[index],
                    onCheckedChange = { checkValue[index] = it }
                )
                Text(stringResource(cuisine.name))
            }
        }
    }
}

@Composable
fun RatingFilter(selected: MutableState<String>) {
    Row {
        ratingLabels.forEachIndexed { index, rating ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                RadioButton(
                    selected = selected.value == rating,
                    onClick = { selected.value = rating }
                )
                Text("$rating+")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.Star, contentDescription = null)
                if (index != (ratingLabels.size - 1)) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerticalDivider(
                        thickness = 2.dp,
                        color = LightGray,
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}
/*
@Composable
fun RatingFilter(filterViewModel: FilterViewModel) {
    // Get the rating options from DataSource
    val ratingOptions = DataSource.ratings // For example: ["2", "3", "4", "5"]
    // Get the current selected rating threshold from the shared ViewModel
    val selectedRating = filterViewModel.selectedRatingThreshold.value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Optionally display the currently selected rating threshold
        Text(text = "Selected Rating: $selectedRating or higher", style = Typography.bodyLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ratingOptions.forEach { rating ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedRating == rating),
                        onClick = { filterViewModel.selectedRatingThreshold.value = rating }
                    )
                    Text(text = rating)
                }
            }
        }
    }
}
 */

@Composable
fun DistanceFilter(selected: MutableState<String>) {
    // Log the currently selected distance each time the composable recomposes.
    Log.d("FILTER_DEBUG", "Currently selected distance: ${selected.value} miles")

    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        distanceOptions.forEach { distance ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(
                    selected = (selected.value == distance),
                    onClick = { selected.value = distance }
                )
                Text(distance)
            }
        }
    }
}
/*
@Composable
fun getSelectedCuisines(
    selectedBooleans: List<Boolean>,
    cuisineList: List<Cuisine>
): List<String> {
    val ctx: Context = LocalContext.current
    return selectedBooleans
        .mapIndexedNotNull { idx, isSelected ->
            if (!isSelected) null
            else {
                // e.g. "Italian" → "italian_restaurant"
                val uiLabel = ctx.getString(cuisineList[idx].name)
                uiLabel
                    .lowercase(Locale.getDefault())
                    .replace(' ', '_') +
                        "_restaurant"
            }
        }
}
*/