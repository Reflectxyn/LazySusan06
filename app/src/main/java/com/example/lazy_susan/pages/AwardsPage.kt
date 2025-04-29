package com.example.lazy_susan.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lazy_susan.R
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed

data class AwardItem(
    val icon: Int,
    val title: String,
    var isExpanded: Boolean = false,
    val description: String,
    var isUnlocked: Boolean = false
)

var items = listOf<AwardItem>(
    AwardItem(icon = R.drawable.award_star, title = "Newcomer", description = "Register to Lazy Susan.", isUnlocked = true),
    AwardItem(icon = R.drawable.star_unfavorited, title = "Opinionated", description = "Favorite a restaurant."),
    AwardItem(icon = R.drawable.history_popup_icon, title = "Aspiring Foodie", description = "Accept 5 restaurants."),
    AwardItem(icon = R.drawable.history_popup_icon, title = "Experienced Foodie", description = "Accept 10 restaurants."),
    AwardItem(icon = R.drawable.history_popup_icon, title = "Master Foodie", description = "Accept 20 restaurants."),
    AwardItem(icon = R.drawable.thumb_down, title = "Picky Lil B*tch", description = "Reject a restaurant.")
)

@Composable
fun AwardsScreen(navController: NavHostController) {
    val expandedStates = remember { mutableStateListOf(*BooleanArray(items.size) { false }.toTypedArray()) }
    val listState = rememberLazyListState()
    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .height(480.dp)
                    .width(376.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(color = HoneyMustardYellow)
                    .padding(start = 32.dp, top = 32.dp, bottom = 52.dp, end = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(174.dp))
                        .background(color = PicnicTableRed)
                        .border(
                            width = 2.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(174.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awards",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    state = listState,
                    modifier = Modifier.height(272.dp)
                ) {
                    itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                        ExpandableAwardItem(
                            item = item,
                            isExpanded = expandedStates[index],
                            onExpandedChange = { expandedStates[index] = it }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(174.dp))
                        .background(color = Color.White)
                        .border(
                            width = 2.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(174.dp)
                        )
                        .clickable{
                            navController.navigateUp()
                        }
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableAwardItem(
    item: AwardItem,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val rotationAngle by animateFloatAsState(targetValue =
        if (isExpanded) {
            180f
        }
        else {
            0f
        }
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .clickable(
                enabled = item.isUnlocked,
                interactionSource = interactionSource,
                indication = null
            ) {
                onExpandedChange(!isExpanded)
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = if(item.isUnlocked) {
                    painterResource(item.icon)
                } else {
                    painterResource(R.drawable.lock)
                },
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if(item.isUnlocked) {
                    item.title
                } else {
                    "Locked"
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription =
                    if (isExpanded) {
                        "Collapse"
                    } else {
                        "Expand"
                    },
                modifier = Modifier.graphicsLayer(rotationZ = rotationAngle)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}