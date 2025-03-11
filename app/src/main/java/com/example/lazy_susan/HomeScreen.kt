package com.example.lazy_susan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import kotlin.math.sqrt

@Composable
fun HomeScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(360.dp)) {
                    val offset = size.height * (2 - sqrt(2.0)) / 4
                    drawCircle(color = PicnicTableRed)
                    drawCircle(
                        color = Color.Black,
                        style = Stroke(width = 10f)
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = size.width / 2, y = 0f),
                        end = Offset(x = size.width / 2, y = size.height),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = 0f, y = size.height / 2),
                        end = Offset(x = size.width, y = size.height / 2),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = offset.toFloat(), y = offset.toFloat()),
                        end = Offset(
                            x = size.width - offset.toFloat(),
                            y = size.height - offset.toFloat()
                        ),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(x = size.width - offset.toFloat(), y = offset.toFloat()),
                        end = Offset(x = offset.toFloat(), y = size.height - offset.toFloat()),
                        strokeWidth = 10f
                    )
                }
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCircle(color = HoneyMustardYellow)
                    drawCircle(color = Color.Black,
                        style = Stroke(width = 10f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = HoneyMustardYellow),
                modifier = Modifier
                    .width(225.dp)
                    .height(65.dp)
                    .border(3.dp, Color.Black, CircleShape)
            ) {
                Text(
                    text = stringResource(R.string.wheel_prompt),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            }
        }
    }
}