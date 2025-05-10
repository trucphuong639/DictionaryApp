package com.example.dictionaryapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.dictionaryapp.prefs.DataStoreManager

class ProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataStoreManager.init(this)
        // Lấy thông tin người dùng từ SharedPreferences
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "User") ?: "User"
        val email = sharedPref.getString("email", "No email") ?: "No email"
        val photoUrl = sharedPref.getString("photo", "") ?: ""

        setContent {
            ProfileScreenContent(name, email, photoUrl)
        }
    }
}

@Composable
fun ProfileScreenContent(userName: String, userEmail: String, userPhotoUrl: String) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf(userPhotoUrl) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it.toString()
            val sharedPref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
            sharedPref.edit().putString("photo", photoUri).apply()
        }
    }

    val purple = Color(0xFF4527A0)
    val noInteraction = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_18),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.65f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(130.dp)) {
                Image(
                    painter = if (photoUri.isNotEmpty()) rememberAsyncImagePainter(photoUri)
                    else painterResource(id = R.drawable.img_15),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, shape = CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_16),
                        contentDescription = "Change Photo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val disabledInteractionSource = remember { MutableInteractionSource() }

            OutlinedTextField(
                value = userName,
                onValueChange = {},
                readOnly = true,
                interactionSource = disabledInteractionSource,
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .pointerInput(Unit) {},
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = purple,
                    unfocusedBorderColor = purple,
                    textColor = purple,
                    focusedLabelColor = purple,
                    unfocusedLabelColor = purple,
                    cursorColor = Color.Transparent,
                    disabledTextColor = purple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                readOnly = true,
                interactionSource = disabledInteractionSource,
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .pointerInput(Unit) {},
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = purple,
                    unfocusedBorderColor = purple,
                    textColor = purple,
                    focusedLabelColor = purple,
                    unfocusedLabelColor = purple,
                    cursorColor = Color.Transparent,
                    disabledTextColor = purple
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val sharedPref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    DataStoreManager.clearUser()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFB39DDB),
                    contentColor = Color.White,
                    disabledBackgroundColor = Color(0xFFEDE7F6)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp,
                    hoveredElevation = 6.dp
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 16.sp
                )
            }
        }
    }
}




