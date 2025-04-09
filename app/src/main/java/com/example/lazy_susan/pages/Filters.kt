package com.example.lazy_susan.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.R
import com.example.lazy_susan.data.DataSource
import com.example.lazy_susan.ui.theme.LightGray
import com.example.lazy_susan.ui.theme.Typography

@Composable
fun FiltersScreen(navController: NavController) {
    var cuisineList = remember {
        mutableStateListOf<Boolean>(false, false, false, false, false, false, false, false)
    }
    var ratingList = remember {
        mutableStateListOf<Boolean>(false, false, false, false)
    }
    var distanceDefault = remember { mutableStateOf("2") }
    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(brush = SolidColor(Color.White), alpha = 0.8f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            CuisineFilter(cuisineList)
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
            RatingFilter(ratingList)
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
                        navController.navigate(AppScreen.Maps.name)
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
                            ratingList.forEachIndexed { index, _ ->
                                ratingList[index] = false
                            }
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
                            TODO()
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
fun RatingFilter(checkValue: MutableList<Boolean>) {
    Row {
        DataSource.ratings.forEachIndexed { index, rating ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checkValue[index],
                    onCheckedChange = { checkValue[index] = it }
                )
                Text(rating)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.Star, contentDescription = null)
                if(index != (DataSource.ratings.size - 1)) {
                    Spacer(modifier = Modifier.width(8.dp))
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
        DataSource.distances.forEach { distance ->
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