package com.example.lazy_susan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.lazy_susan.ui.theme.HoneyMustardYellow
import com.example.lazy_susan.ui.theme.PicnicTableRed
import kotlin.math.sqrt

@Composable
fun HomeScreen() {
    val list = listOf("1", "2", "3", "4")
    var result = remember { mutableStateOf("") }
    val showResult = remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Wheel()
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    result.value = list.random()
                    showResult.value = true
                },
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
    if(showResult.value) {
        Result(showResult, result)
    }
}

@Composable
fun Wheel() {
    val painterFire = rememberVectorPainter(ImageVector.vectorResource(R.drawable.star))
    val painterFunnel = rememberVectorPainter(ImageVector.vectorResource(R.drawable.filter))
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
    Box {
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(color = HoneyMustardYellow)
            drawCircle(
                color = Color.Black,
                style = Stroke(width = 10f)
            )
            with(painterFire) {
                draw(size = Size(140.dp.toPx(), 140.dp.toPx()))
            }
        }
    }
    Box(
        modifier = Modifier.clickable {

        }
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            translate(left = 420f, top = -504f) {
                drawCircle(color = HoneyMustardYellow)
                drawCircle(
                    color = Color.Black,
                    style = Stroke(width = 8f)
                )
            }
        }
        Canvas(modifier = Modifier.size(64.dp)) {
            translate(left = 442f, top = -478f) {
                with(painterFunnel) {
                    draw(size = Size(48.dp.toPx(), 48.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun Result(showResult: MutableState<Boolean>, result: MutableState<String>) {
    Dialog(onDismissRequest = {showResult.value = false}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = result.value)
        }
    }
}