package com.example.lazy_susan.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.PresetViewModel
import com.example.lazy_susan.PresetViewModelFactory
import com.example.lazy_susan.R

@Composable
fun PresetPage(
    userId: String, // pass from AuthViewModel when logged in
    navController: NavController,
    PresetViewModel: PresetViewModel =
        viewModel(factory = PresetViewModelFactory(userId))
) {

    val presets by PresetViewModel.presets.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        PresetViewModel.fetchPresets()
    }

    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        presets.forEachIndexed { index, preset ->
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = preset.name,
                    onValueChange = { newName ->
                        PresetViewModel.updatePresetName(preset.id, newName)
                    },
                    label = { Text("Preset ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = {navController.navigate("filters/${preset.id}")
                },

                ) {
                    Text(text = "Edit")
                }

                Button(onClick = { PresetViewModel.copyPreset(preset.id) }) {
                    Text(text = "copy")
                }

                Button(onClick = { PresetViewModel.deletePreset(preset.id) }) {
                    Text(text = "Delete")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(AppScreen.Filters.name) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add +")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {navController.navigate(route = AppScreen.ProfileHome.name)},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Done")
        }
    }


}

