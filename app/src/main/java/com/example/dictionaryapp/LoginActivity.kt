package com.example.dictionaryapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dictionaryapp.model.User
import com.example.dictionaryapp.prefs.DataStoreManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest

class LoginActivity : ComponentActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var callbackManager: CallbackManager

    private lateinit var launchernew: ActivityResultLauncher<IntentSenderRequest>

    private val RC_SIGN_IN = 59674829
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.result
                    if (account != null) {
                        // Lưu thông tin Google vào SharedPreferences
                        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("name", account.displayName ?: "User")
                            .putString("email", account.email ?: "No email")
                            .putString("photo", account.photoUrl?.toString() ?: "")
                            .apply()

                        val user = User(
                            name = account.displayName,
                            email = account.email,
                            pictureUrl = account.photoUrl?.toString() ?: ""
                        )

                        DataStoreManager.setUser(user)

                        // Chuyển sang màn hình LanguageSelectionActivity
                        Toast.makeText(this, "Google Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LanguageSelectionActivity::class.java))
                        finish()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataStoreManager.init(this)
        val user = DataStoreManager.getUser()
        if(user != null && user.name != null && user.email != null) {
            Log.d("User Info", "Username: ${user.name}, Email: ${user.email}, Photo: ${user.pictureUrl}")
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
            finish()
        }

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        callbackManager = CallbackManager.Factory.create()

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("836300900402-t7u6vee60orr854qhpp5bnfii47s1dh9.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val username = credential.displayName
                val email = credential.id
                val profilePicUrl = credential.profilePictureUri?.toString() ?: ""

                val user = User(
                    name = username,
                    email = email,
                    pictureUrl = profilePicUrl
                )

                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                sharedPref.edit()
                    .putString("name", username ?: "User")
                    .putString("email", email ?: "No email")
                    .putString("photo", profilePicUrl)
                    .apply()

                DataStoreManager.setUser(user)


                Toast.makeText(this, "Login as $username", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, LanguageSelectionActivity::class.java))
                finish()
            } catch (e: ApiException) {
                Toast.makeText(this, "Google login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(this, "Please fill in all the information", Toast.LENGTH_SHORT).show()
                        return@LoginScreen
                    }

                    val auth = FirebaseAuth.getInstance()
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Lưu thông tin người dùng vào SharedPreferences
                                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)

                                val userid = auth.uid;
                                var stringname = "";

                                System.out.println("OutUserid: "+ userid);

                                getUsernameFromDatabase(userid.toString()) { username ->
                                    System.out.println("loggggggggggg to func: "+ username)
                                        // Username is successfully retrieved, use it here
                                    if (username != null) {

                                        sharedPref.edit()
                                            .putString("name", username)  // Tạm dùng phần tên từ email
                                            .putString("email", email)
                                            .putString("photo", "")  // Không có ảnh cho email login
                                            .apply()

                                        val user = User(
                                            name =username,  // Tạm dùng phần tên từ email
                                            email = email,
                                            pictureUrl = ""  // Không có ảnh cho email login
                                        )
                                        DataStoreManager.setUser(user)
                                    };
                                }

                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LanguageSelectionActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                onGoogleClick = {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            try {
                                launcher.launch(
                                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                )
                            } catch (e: Exception) {
                                Toast.makeText(this, "Launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                onFacebookClick = {
                    LoginManager.getInstance()
                        .logInWithReadPermissions(this, listOf("public_profile", "email"))
                    LoginManager.getInstance()
                        .registerCallback(callbackManager,
                            object : FacebookCallback<LoginResult> {
                                override fun onSuccess(result: LoginResult) {
                                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                                    FirebaseAuth.getInstance().signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val user = FirebaseAuth.getInstance().currentUser
                                                val graphRequest = GraphRequest.newMeRequest(result.accessToken) { _, response ->
                                                    val pictureUrl = response?.getJSONObject()?.getJSONObject("picture")
                                                        ?.getJSONObject("data")
                                                        ?.getString("url")

                                                    val email = user?.email
                                                    val name = user?.displayName

                                                    // Lưu thông tin người dùng vào SharedPreferences
                                                    val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                                                    sharedPref.edit()
                                                        .putString("name", name ?: "")
                                                        .putString("email", email ?: "")
                                                        .putString("photo", pictureUrl ?: "")
                                                        .apply()
                                                    val user = User(
                                                        name = name,  // Tạm dùng phần tên từ email
                                                        email = email,
                                                        pictureUrl = ""  // Không có ảnh cho email login
                                                    )
                                                    DataStoreManager.setUser(user)

                                                    // Chuyển sang màn hình LanguageSelectionActivity (giữ như cũ)
                                                    startActivity(Intent(this@LoginActivity, LanguageSelectionActivity::class.java))
                                                    finish()
                                                }
                                                val parameters = Bundle()
                                                parameters.putString("fields", "id,name,email,picture")
                                                graphRequest.parameters = parameters
                                                graphRequest.executeAsync()
                                            } else {
                                                Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }

                                override fun onCancel() {
                                    Toast.makeText(this@LoginActivity, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show()
                                }

                                override fun onError(error: FacebookException) {
                                    Toast.makeText(this@LoginActivity, "Facebook Login Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                }
            )
        }
    }

    fun getUsernameFromDatabase(userId: String, callback: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

        // Get the username for the given userId
        myRef.child("users").child(userId).child("username").get()
            .addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.value as? String
                callback(username)  // Passing the result to the callback
            }
            .addOnFailureListener { exception ->
                callback(null)  // Passing null in case of failure
                Toast.makeText(this, "Failed to get username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        System.out.println("requestCode: "+ requestCode);

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Nếu đăng nhập thành công
                val account = task.result
                val username = account.displayName // Tên người dùng
                val email = account.email // Địa chỉ email
                val profilePicUrl = account.photoUrl.toString() // URL ảnh đại diện (nếu có)

                val user = User(
                    name = username,
                    email = email,
                    pictureUrl = profilePicUrl
                )

                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                sharedPref.edit()
                    .putString("name", username ?: "User")
                    .putString("email", email ?: "No email")
                    .putString("photo", profilePicUrl)
                    .apply()
                DataStoreManager.setUser(user)
                Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Xử lý khi có lỗi
                Toast.makeText(this, "Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val avatarSize = screenWidth * 0.45f
    val textSize = screenWidth.value * 0.08f
    val fieldFontSize = screenWidth.value * 0.045f
    val buttonFontSize = screenWidth.value * 0.055f
    val socialIconSize = screenWidth * 0.14f
    val spacing = screenHeight * 0.015f

    var showDialog by remember { mutableStateOf(false) }

    // Hiển thị Dialog nếu showDialog là true
    if (showDialog) {
        ForgotPasswordDialog(
            onDismiss = { showDialog = false },
            onSubmit = { email ->
                sendPasswordResetEmail(email)
                showDialog = false
            }
        )
    }

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
                .padding(horizontal = screenWidth * 0.07f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.06f))

            Image(
                painter = painterResource(id = R.drawable.img_15),
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(spacing))

            Text(
                text = "SIGN IN",
                fontSize = textSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(spacing * 2))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "Email",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = fieldFontSize.sp
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = fieldFontSize.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(spacing))

            var showPassword by remember { mutableStateOf(false) } // Trạng thái hiển thị mật khẩu

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "Password",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = fieldFontSize.sp
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = fieldFontSize.sp
                ),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    backgroundColor = Color.Transparent
                ),
                singleLine = true,
                trailingIcon = {
                    val eyeIcon = if (showPassword) R.drawable.img_8 else R.drawable.img_7
                    val description = if (showPassword) "Hide password" else "Show password"

                    IconButton(onClick = { showPassword = !showPassword }) {
                        if (showPassword) {
                            Icon(
                                painter = painterResource(id = R.drawable.img_8),
                                contentDescription = description,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.img_7),
                                contentDescription = description,
                                tint = Color.White
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(spacing * 0.8f))

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            Text(
                text = "Forgot your password?",
                fontStyle = FontStyle.Italic,
                fontSize = fieldFontSize.sp,
                color = if (isPressed) Color.White else Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(interactionSource = interactionSource, indication = null) {
                        showDialog = true // Khi nhấn, mở dialog
                    }
            )

            Spacer(modifier = Modifier.height(spacing * 2))

            Button(
                onClick = { onLoginClick(email, password) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.07f),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = buttonFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE7A8C8)
                )
            }

            Spacer(modifier = Modifier.height(spacing * 1.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.weight(1f))
                Text("  Or  ", fontSize = fieldFontSize.sp, color = Color.White)
                Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(spacing))

            Row {
                Image(
                    painter = painterResource(id = R.drawable.img_2),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(socialIconSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(socialIconSize * 0.2f)
                        .clickable { onGoogleClick() }
                )

                Spacer(modifier = Modifier.width(spacing))

                Image(
                    painter = painterResource(id = R.drawable.img_3),
                    contentDescription = "Facebook",
                    modifier = Modifier
                        .size(socialIconSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(socialIconSize * 0.2f)
                        .clickable { onFacebookClick() }
                )
            }

            Spacer(modifier = Modifier.height(spacing))

            val signUpInteraction = remember { MutableInteractionSource() }
            val isSignUpPressed by signUpInteraction.collectIsPressedAsState()

            Row {
                Text(
                    text = "Don't have an account?",
                    fontSize = fieldFontSize.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(4.dp))

                val context = LocalContext.current
                Text(
                    text = "Sign Up",
                    fontSize = fieldFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE7A8C8),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(
                        interactionSource = signUpInteraction,
                        indication = null
                    ) {
                        context.startActivity(Intent(context, SignupActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun ForgotPasswordText() {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Trạng thái để mở Dialog
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ForgotPasswordDialog(
            onDismiss = { showDialog = false },
            onSubmit = { email ->
                sendPasswordResetEmail(email)
                showDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Forgot your password?",
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            color = if (isPressed) Color.White else Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(interactionSource = interactionSource, indication = null) {
                    // Khi click vào, mở dialog nhập email
                    showDialog = true
                }
        )
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Please enter your email address:")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onSubmit(email)  // Gọi onSubmit để xử lý gửi email
                        Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Gửi email reset mật khẩu qua Firebase
fun sendPasswordResetEmail(email: String) {
    val auth = FirebaseAuth.getInstance()

    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("ForgotPassword", "Reset email sent successfully.")
            } else {
                Log.e("ForgotPassword", "Failed to send reset email: ${task.exception?.message}")
            }
        }
}




