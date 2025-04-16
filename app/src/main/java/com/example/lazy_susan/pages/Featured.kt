package com.example.lazy_susan
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

                    if (restaurantName != null && isFavorited) {
                        Restaurant(restaurantName, address, phoneNumber, hours, restaurantId, isFavorited)
                    } else null
                }
                favoriteListFlow.update { favorites }
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
                if (favoriteList.isEmpty()) {
                    Text(
                        text = "No favorites yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    favoriteList.forEachIndexed { index, restaurant ->
                        RestaurantItem(
                            restaurant = restaurant,
                            userId = userId,
                            showDialog = currentPopupIndex.value == index,
                            onShowDialog = { currentPopupIndex.value = index },
                            onDismissDialog = { currentPopupIndex.value = -1 },
                            onNavigate = { direction ->
                                val newIndex = when (direction) {
                                    "left" -> (currentPopupIndex.value - 1 + favoriteList.size) % favoriteList.size
                                    "right" -> (currentPopupIndex.value + 1) % favoriteList.size
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







