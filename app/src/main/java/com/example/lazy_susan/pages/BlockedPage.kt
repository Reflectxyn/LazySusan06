package com.example.lazy_susan.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.RestaurantItem
import com.google.firebase.database.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.lazy_susan.AppScreen

@Composable
fun BlockedScreen(userId: String, navController: NavController) {
    val db = FirebaseDatabase.getInstance().reference
    val blockedListFlow = remember { MutableStateFlow<List<Restaurant>>(emptyList()) }
    val currentPopupIndex = remember { mutableStateOf(-1) }

    DisposableEffect(userId) {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blockedRestaurants = snapshot.children.mapNotNull { child ->
                    val isBlocked = child.child("blocked").getValue(Boolean::class.java) ?: false
                    if (isBlocked) {
                        val name = child.child("name").getValue(String::class.java)
                        val address = child.child("address").getValue(String::class.java) ?: "Address not found"
                        val phone = child.child("phoneNumber").getValue(String::class.java) ?: "Phone number not found"
                        val hours = child.child("hours").getValue(String::class.java) ?: "Hours not found"
                        val id = child.child("id").getValue(String::class.java) ?: "No ID"
                        val rating = child.child("rating").getValue(Double::class.java) ?: 0.0
                        val types = child.child("types").children.mapNotNull { it.getValue(String::class.java) }
                        val lat = child.child("latitude").getValue(Double::class.java)
                        val lng = child.child("longitude").getValue(Double::class.java)

                        name?.let {
                            Restaurant(
                                name = it,
                                address = address,
                                phoneNumber = phone,
                                hours = hours,
                                id = id,
                                rating = rating,
                                types = types,
                                isBlocked = true,
                                latitude = lat,
                                longitude = lng
                            )
                        }
                    } else null
                }
                blockedListFlow.update { blockedRestaurants }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle error
            }
        }

        restaurantRef.addValueEventListener(listener)
        onDispose {
            restaurantRef.removeEventListener(listener)
        }
    }

    val blockedList by blockedListFlow.collectAsState()

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
            Button(
                onClick = { navController.navigate(route = AppScreen.ProfileHome.name)},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Back to Profile")
            }
            if (blockedList.isEmpty()) {
                Text(
                    text = "No blocked restaurants.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                blockedList.forEachIndexed { index, restaurant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                db.child("users")
                                    .child(userId)
                                    .child("userRestaurants")
                                    .child(restaurant.id)
                                    .removeValue()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Unblock")
                        }
                        IconButton(onClick = { currentPopupIndex.value = index }) {
                            Icon(
                                painter = painterResource(id = R.drawable.history_popup_icon),
                                contentDescription = "More Info"
                            )
                        }
                    }

                    // Dialog popup
                    if (currentPopupIndex.value == index) {
                        AlertDialog(
                            onDismissRequest = { currentPopupIndex.value = -1 },
                            title = {
                                Text(text = restaurant.name, style = MaterialTheme.typography.titleLarge)
                            },
                            text = {
                                Column {
                                    Text("Address: ${restaurant.address}")
                                    Text("Phone: ${restaurant.phoneNumber}")
                                    Text("Hours: ${restaurant.hours}")
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { currentPopupIndex.value = -1 }) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}