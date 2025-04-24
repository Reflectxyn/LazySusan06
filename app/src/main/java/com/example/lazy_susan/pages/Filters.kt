package com.example.lazy_susan.pages

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
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
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.Filters
import com.example.lazy_susan.PresetViewModel
import com.example.lazy_susan.PresetViewModelFactory
import com.example.lazy_susan.R
import com.example.lazy_susan.ui.theme.LightGray
import com.example.lazy_susan.ui.theme.Typography


val cuisineLabels = listOf("Italian", "Japanese", "Thai", "Mexican", "Indian", "Chinese", "Greek", "American")
val ratingLabels = listOf("2", "3", "4", "5")
val distanceOptions = listOf("1", "2", "5", "10", "15")

@Composable
fun FiltersScreen(
    navController: NavController,
    userId: String,
    presetId: String = "",
    presetViewModel: PresetViewModel =
        viewModel(factory = PresetViewModelFactory(userId))
) {
    val preset = presetViewModel.presets.observeAsState(emptyList()).value.find { it.id == presetId }

    val cuisineList = remember { mutableStateListOf(*Array(cuisineLabels.size) { false }) }
    val ratingDefault = remember { mutableStateOf("3") }
    val distanceDefault = remember { mutableStateOf("2") }

    LaunchedEffect(preset) {
        preset?.let {
            it.filters.cuisines.forEachIndexed { index, value -> cuisineList[index] = value }
            ratingDefault.value = it.filters.rating.toString()
            distanceDefault.value = it.filters.distance.toString()
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
            itemsIndexed(cuisineLabels) { index, cuisine ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 44.dp)
                ) {
                    Checkbox(
                        checked = cuisineList[index],
                        onCheckedChange = { cuisineList[index] = it }
                    )
                    Text(text = cuisine, modifier = Modifier.width(100.dp))
                }
            }
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
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                navController.navigate("${AppScreen.Maps.name}/${distanceDefault.value}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Maps",
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
fun RatingFilter(selected: MutableState<String>) {
    Row {
        ratingLabels.forEachIndexed { index, rating ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                RadioButton(
                    selected = (selected.value == rating),
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

@Composable
fun DistanceFilter(selected: MutableState<String>) {
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

