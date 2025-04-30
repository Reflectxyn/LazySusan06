package com.example.lazy_susan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


@Composable
fun HistoryScreen(userId: String) {
    val db = FirebaseDatabase.getInstance().reference
    val restaurantListFlow = remember { MutableStateFlow<List<Restaurant>>(emptyList()) }
    val currentPopupIndex = remember { mutableStateOf(-1) }

    DisposableEffect(userId) {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTime = System.currentTimeMillis()
                val thirtyDays = 30L * 24 * 60 * 60 * 1000

                val restaurants = snapshot.children.mapNotNull { child ->
                    val restaurantName = child.child("name").getValue(String::class.java)
                    val address = child.child("address").getValue(String::class.java) ?: "Address not found"
                    val phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: "Phone number not found"
                    val hours = child.child("hours").getValue(String::class.java) ?: "Hours not found"
                    val isFavorited = child.child("isFavorited").getValue(Boolean::class.java) ?: false
                    val isBlocked = child.child("blocked").getValue(Boolean::class.java) ?: false
                    val restaurantId = child.child("id").getValue(String::class.java) ?: "No id"
                    val timestamp = child.child("timestamp").getValue(Long::class.java)

                    val lat = child.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = child.child("longitude").getValue(Double::class.java) ?: 0.0

                    if (restaurantName != null && timestamp != null && !isBlocked) {
                        if (timestamp >= (currentTime - thirtyDays)) {
                            Restaurant(
                                name = restaurantName,
                                address = address,
                                phoneNumber = phoneNumber,
                                hours = hours,
                                id = restaurantId,
                                isFavorited = isFavorited,
                                isBlocked = isBlocked,
                                latitude    = lat,      // ← CHANGED: pass latitude
                                longitude   = lng       // ← CHANGED: pass longitude

                            )
                        } else null
                    } else null
                }
                restaurantListFlow.update { restaurants }
            }

            override fun onCancelled(error: DatabaseError) {
                // set up Toast to handle database errors
            }
        }
        restaurantRef.addValueEventListener(listener)

        onDispose {
            restaurantRef.removeEventListener(listener)
        }
    }

    val restaurantList by restaurantListFlow.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (restaurantList.isEmpty()) {
                    Text(
                        text = "No restaurant history available.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    restaurantList.forEachIndexed { index, restaurant ->
                        RestaurantItem(
                            restaurant = restaurant,
                            userId = userId,
                            showDialog = currentPopupIndex.value == index,
                            onShowDialog = { currentPopupIndex.value = index },
                            onDismissDialog = { currentPopupIndex.value = -1 },
                            onNavigate = { direction ->
                                val newIndex = when (direction) {
                                    "left" -> (currentPopupIndex.value - 1 + restaurantList.size) % restaurantList.size
                                    "right" -> (currentPopupIndex.value + 1) % restaurantList.size
                                    else -> currentPopupIndex.value
                                }
                                currentPopupIndex.value = newIndex
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .background(Color.White, shape = CircleShape)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Disclaimer: Past results will be archived after 30 days",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantItem(
    restaurant: Restaurant,
    userId: String,
    showDialog: Boolean = false,
    onShowDialog: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val db = FirebaseDatabase.getInstance().reference
    var isFavorited by remember { mutableStateOf(restaurant.isFavorited) }

    DisposableEffect(userId, restaurant.id) {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants").child(restaurant.id)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedIsFavorited = snapshot.child("isFavorited").getValue(Boolean::class.java) ?: false
                isFavorited = updatedIsFavorited
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        restaurantRef.addValueEventListener(listener)

        onDispose {
            restaurantRef.removeEventListener(listener)
        }
    }

    fun toggleFavorite() {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants").child(restaurant.id)
        restaurantRef.child("isFavorited").setValue(!isFavorited)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { toggleFavorite() }) {
                Icon(
                    painter = painterResource(if (isFavorited) R.drawable.star_favorited else R.drawable.star_unfavorited),
                    contentDescription = if (isFavorited) "Unfavorite" else "Favorite",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontSize = 24.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onShowDialog() }) {
                Icon(
                    painter = painterResource(id = R.drawable.history_popup_icon),
                    contentDescription = "More Info"
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismissDialog() },
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, shape = CircleShape)
                        .background(Color.White, shape = CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        fontSize = 28.sp
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    InfoBoxWithIcon(R.drawable.history_popup_icon, restaurant.address)
                    InfoBoxWithIcon(R.drawable.clock_icon, restaurant.hours)
                    InfoBoxWithIcon(R.drawable.phone_icon, restaurant.phoneNumber)
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { onDismissDialog() }) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(65.dp)
                                .border(1.dp, Color.Black, CircleShape)
                                .shadow(4.dp, shape = CircleShape)
                                .background(Color.White, shape = CircleShape)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Done", fontSize = 24.sp, color = Color.Black)
                        }
                    }
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { onNavigate("left") }) {
                        Icon(painter = painterResource(R.drawable.left_arrow), contentDescription = "Previous", modifier = Modifier.size(60.dp))
                    }
                    IconButton(onClick = { onNavigate("right") }) {
                        Icon(painter = painterResource(R.drawable.right_arrow), contentDescription = "Next", modifier = Modifier.size(60.dp))
                    }
                }
            },
            containerColor = Color(0xFFF0F0F0)
        )
    }
}

@Composable
fun InfoBoxWithIcon(iconRes: Int, infoText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFD3D3D3), shape = CircleShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = infoText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}