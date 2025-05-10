package com.example.dictionaryapp

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Context
import com.google.gson.Gson

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignupScreen(
                onLoginClick = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

// Hàm lưu username vào Firebase Realtime Database
fun saveUsernameToDatabase(userId: String, username: String , context : android.content.Context) {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.reference

    // Lưu username theo userId vào Realtime Database
    myRef.child("users").child(userId).child("username").setValue(username)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Username saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Failed to save username: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}


@Composable
fun SignupScreen(
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordMismatch by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "SIGN UP",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Name", color = Color.White.copy(alpha = 0.7f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = textFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = textFieldColors(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordMismatch = confirmPassword.isNotEmpty() && confirmPassword != it
                },
                placeholder = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = textFieldColors(),
                singleLine = true,
                trailingIcon = {
                    val iconRes = if (showPassword) R.drawable.img_8 else R.drawable.img_7
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordMismatch = it != password
                },
                placeholder = { Text("Confirm Password", color = Color.White.copy(alpha = 0.7f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = textFieldColors(),
                singleLine = true,
                trailingIcon = {
                    val iconRes = if (showConfirmPassword) R.drawable.img_8 else R.drawable.img_7
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )

            if (passwordMismatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        passwordMismatch = true
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // ✅ Lưu email & password vào SharedPreferences
                                    val sharedPref = context.getSharedPreferences("UserData", MODE_PRIVATE)
                                    val gson = Gson()

                                    // Lấy danh sách tài khoản đã lưu trước đó
                                    val existingAccountsJson = sharedPref.getString("accounts", null)
                                    val accountList: MutableList<UserAccount> = if (existingAccountsJson != null) {
                                        val type = object : TypeToken<MutableList<UserAccount>>() {}.type
                                        gson.fromJson(existingAccountsJson, type)
                                    } else {
                                        mutableListOf()
                                    }

                                    // Thêm tài khoản mới nếu chưa có
                                    if (accountList.none { it.email == email }) {
                                        accountList.add(UserAccount(email, password))
                                        val newJson = gson.toJson(accountList)
                                        sharedPref.edit().putString("accounts", newJson).apply()
                                    }

                                    val userId = auth.currentUser?.uid ?: ""
                                    val username = name // Sử dụng tên mà người dùng đã nhập làm username
                                    if (userId.isNotEmpty()) {
                                        saveUsernameToDatabase(userId, username , context)
                                    }


                                    Toast.makeText(context, "Sign up successful. Please login.", Toast.LENGTH_SHORT).show()
                                    onLoginClick()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Sign up failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE7A8C8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val loginInteraction = remember { MutableInteractionSource() }

            Row {
                Text(text = "Already have an account?", fontSize = 19.sp, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Login",
                    fontSize = 19.sp,
                    color = Color(0xFFE7A8C8),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(
                        interactionSource = loginInteraction,
                        indication = null
                    ) {
                        onLoginClick()
                    }
                )
            }
        }
    }
}

data class UserAccount(val email: String, val password: String)


@Composable
fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
    cursorColor = Color.White,
    backgroundColor = Color.Transparent
)
