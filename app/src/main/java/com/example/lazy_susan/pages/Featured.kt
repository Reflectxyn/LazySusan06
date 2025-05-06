package com.example.lazy_susan.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun FeaturedScreen(userId: String) {
    val db = FirebaseDatabase.getInstance().reference
    val favoriteListFlow = remember { MutableStateFlow<List<Restaurant>>(emptyList()) }

    DisposableEffect(userId) {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = snapshot.children.mapNotNull { child ->
                    val restaurantName = child.child("name").getValue(String::class.java)
                    val address = child.child("address").getValue(String::class.java) ?: "Address not found"
                    val phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: "Phone number not found"
                    val hours = child.child("hours").getValue(String::class.java) ?: "Hours not found"
                    val isFavorited = child.child("isFavorited").getValue(Boolean::class.java) ?: false
                    val restaurantId = child.child("id").getValue(String::class.java) ?: "No id"
                    val timestamp = child.child("timestamp").getValue(Long::class.java)?: 0L

                    // ← CHANGED: pull latitude & longitude from your DB
                    val lat = child.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = child.child("longitude").getValue(Double::class.java) ?: 0.0

                    if (restaurantName != null && isFavorited) {
                        Restaurant(
                            name = restaurantName,
                            address = address,
                            phoneNumber = phoneNumber,
                            hours = hours,
                            id = restaurantId,
                            isFavorited = isFavorited,
                            latitude = lat,    // ← CHANGED: pass latitude
                            longitude = lng,     // ← CHANGED: pass longitude
                            timestamp = timestamp
                        )
                    } else null
                }

                val uniqueFavorites = favorites.distinctBy { it.name to it.address }
                favoriteListFlow.update { uniqueFavorites }
                favoriteListFlow.update { uniqueFavorites }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        restaurantRef.addValueEventListener(listener)

        onDispose {
            restaurantRef.removeEventListener(listener)
        }
    }

    val favoriteList by favoriteListFlow.collectAsState()
    val reversedList = favoriteList.reversed()
    val currentPopupIndex = remember { mutableStateOf(-1) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    if (reversedList.isEmpty()) {
                        Text(
                            text = "No favorites yet.",
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        LazyColumn {
                            itemsIndexed(
                                reversedList,
                                key = { index, _ -> index }) { index, restaurant ->
                                RestaurantItem(
                                    restaurant = restaurant,
                                    page = AppScreen.Featured.name,
                                    userId = userId,
                                    showDialog = currentPopupIndex.value == index,
                                    onShowDialog = { currentPopupIndex.value = index },
                                    onDismissDialog = { currentPopupIndex.value = -1 },
                                    onNavigate = { direction ->
                                        val newIndex = when (direction) {
                                            "left" -> (currentPopupIndex.value - 1 + reversedList.size) % reversedList.size
                                            "right" -> (currentPopupIndex.value + 1) % reversedList.size
                                            else -> currentPopupIndex.value
                                        }
                                        currentPopupIndex.value = newIndex
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}