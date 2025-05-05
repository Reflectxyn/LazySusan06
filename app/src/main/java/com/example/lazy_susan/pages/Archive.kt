package com.example.lazy_susan.pages

import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.RestaurantItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember



@Composable
fun ArchiveScreen(userId: String) {
    val db = FirebaseDatabase.getInstance().reference
    val archiveListFlow = remember { MutableStateFlow<List<Restaurant>>(emptyList()) }

    DisposableEffect(userId) {
        val restaurantRef = db.child("users").child(userId).child("userRestaurants")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTime = System.currentTimeMillis()
                val ninetyDays = 90L * 24 * 60 * 60 * 1000
                val thirtyDays = 30L * 24 * 60 * 60 * 1000

                val archive = snapshot.children.mapNotNull { child ->
                    val restaurantName = child.child("name").getValue(String::class.java) ?: "Name not found"
                    val address = child.child("address").getValue(String::class.java) ?: "Address not found"
                    val phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: "Phone number not found"
                    val hours = child.child("hours").getValue(String::class.java) ?: "Hours not found"
                    val isFavorited = child.child("isFavorited").getValue(Boolean::class.java) ?: false
                    val restaurantId = child.child("id").getValue(String::class.java) ?: "No id"
                    val timestamp = child.child("timestamp").getValue(Long::class.java)?: 0L

                    // ← CHANGED: pull latitude & longitude from your DB
                    val lat = child.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = child.child("longitude").getValue(Double::class.java) ?: 0.0

                    // to remove restaurants that exceed 90 days from database
                    if (timestamp != null && timestamp < (currentTime - ninetyDays)) {
                        child.ref.removeValue()
                        return@mapNotNull null
                    }

                    if (timestamp >= (currentTime - ninetyDays) && timestamp < (currentTime - thirtyDays)){
                        Restaurant(
                            name        = restaurantName,
                            address     = address,
                            phoneNumber = phoneNumber,
                            hours       = hours,
                            id          = restaurantId,
                            isFavorited = isFavorited,
                            latitude    = lat,    // ← CHANGED: pass latitude
                            longitude   = lng,     // ← CHANGED: pass longitude
                            timestamp = timestamp
                        )
                    } else null
                }


                archiveListFlow.update { archive }
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

    val archiveList by archiveListFlow.collectAsState()
    val currentPopupIndex = remember { mutableStateOf(-1) }

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
                if (archiveList.isEmpty()) {
                    Text(
                        text = "Archive is empty.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    archiveList.forEachIndexed { index, restaurant ->
                        RestaurantItem(
                            restaurant = restaurant,
                            userId = userId,
                            showDialog = currentPopupIndex.value == index,
                            onShowDialog = { currentPopupIndex.value = index },
                            onDismissDialog = { currentPopupIndex.value = -1 },
                            onNavigate = { direction ->
                                val newIndex = when (direction) {
                                    "left" -> (currentPopupIndex.value - 1 + archiveList.size) % archiveList.size
                                    "right" -> (currentPopupIndex.value + 1) % archiveList.size
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











