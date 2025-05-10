package com.example.dictionaryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StartScreen {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Kết thúc MainActivity để không quay lại bằng nút back
            }
        }
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(3000)
        onStart()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hình nền
        Image(
            painter = painterResource(id = R.drawable.img_9),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 270.dp, height = 270.dp)
                .clip(RoundedCornerShape(30.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

