package com.example.lazy_susan.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lazy_susan.AppScreen
import com.example.lazy_susan.R
import com.example.lazy_susan.Restaurant
import com.example.lazy_susan.ui.theme.Typography
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun ArchiveScreen(userId: String, navController: NavController) {
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
    val reversedList = archiveList.reversed()
    val currentPopupIndex = remember { mutableStateOf(-1) }

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
                if (reversedList.isEmpty()) {
                    Text(
                        text = "Archive is empty.",
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(0.dp, 556.dp)) {
                        itemsIndexed(
                            reversedList,
                            key = { index, _ -> index }) { index, restaurant ->
                            RestaurantItem(
                                restaurant = restaurant,
                                page = AppScreen.Archive.name,
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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Disclaimer:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                    fontSize = 16.sp
                )
                Text(
                    text = "Old orders will be deleted after 80 days",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                    fontSize = 16.sp
                )
                Box(
                    contentAlignment = Alignment.Center,
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
                                navController.navigate(route = AppScreen.History.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Go Back",
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











