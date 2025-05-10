package com.example.dictionaryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

class LanguageSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy tên người dùng được truyền từ LoginActivity (Google hoặc Facebook)
        val userName = intent.getStringExtra("USERNAME") ?: "User"

        setContent {
            val userName = intent.getStringExtra("username")
            LanguageSelectionScreen(userName = userName)
        }
    }
}

@Composable
fun LanguageSelectionScreen(userName: String? = null) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.img_10),
            contentDescription = "App Illustration",
            modifier = Modifier
                .size(350.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "'Tra cứu từ vựng nhanh chóng, ví dụ rõ ràng, phát âm chuẩn và dịch nghĩa sang tiếng Việt. Hy vọng điều này sẽ mang lại cho bạn một trải nghiệm tốt!'",
            fontSize = 20.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                val intent = Intent(context, MainDictionaryActivity::class.java)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9370DB)),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Text(text = "Bắt đầu ngay", color = Color.White, fontSize = 18.sp)
        }
    }
}
