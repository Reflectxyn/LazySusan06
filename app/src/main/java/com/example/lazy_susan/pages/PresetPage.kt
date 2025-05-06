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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.PresetViewModel
import com.example.lazy_susan.PresetViewModelFactory
import com.example.lazy_susan.R
import com.example.lazy_susan.ui.theme.Typography

@Composable
fun PresetPage(
    userId: String, // pass from AuthViewModel when logged in
    navController: NavController,
    PresetViewModel: PresetViewModel =
        viewModel(factory = PresetViewModelFactory(userId))
) {

    val presets by PresetViewModel.presets.observeAsState(emptyList())
    val reversedList = presets.reversed()

    LaunchedEffect(Unit) {
        PresetViewModel.fetchPresets()
    }

    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(brush = SolidColor(Color.White), alpha = 0.8f)
        ) {
            Box(modifier = Modifier
                .padding(
                    top = 20.dp,
                    bottom = 10.dp,
                    start = 20.dp,
                    end = 20.dp
                )
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(556.dp)
                ) {
                    itemsIndexed(
                        reversedList,
                        key = { index, _ -> index }
                    ) { index, preset ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(width = 1.5.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .background(color = Color.White)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextField(
                                value = preset.name,
                                onValueChange = { newName ->
                                    PresetViewModel.updatePresetName(preset.id, newName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                textStyle = TextStyle.Default.copy(fontSize = 18.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(
                                onClick = { navController.navigate("filters/${preset.id}") },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .size(32.dp)
                                    .background(Color.White)
                            ) {
                                Icon(
                                    painterResource(R.drawable.edit),
                                    contentDescription = "Edit"
                                )
                            }
                            IconButton(
                                onClick = { PresetViewModel.copyPreset(preset.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .size(32.dp)
                                    .background(Color.White)
                            ) {
                                Icon(painterResource(R.drawable.copy), contentDescription = "Copy")
                            }
                            IconButton(
                                onClick = { PresetViewModel.deletePreset(preset.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .size(32.dp)
                                    .background(Color.White)
                            ) {
                                Icon(
                                    painterResource(R.drawable.delete),
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color = Color.Black)
                        .clickable {
                            navController.navigate(route = AppScreen.Filters.name)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add +",
                        style = Typography.bodyLarge,
                        color = Color.White
                    )
                }
                    Box(
                        modifier = Modifier
                            .width(104.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color.Black)
                            .clickable {
                                navController.navigate(route = AppScreen.ProfileHome.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            style = Typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

