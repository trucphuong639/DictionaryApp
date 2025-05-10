package com.example.dictionaryapp

import android.content.Context
import android.os.Bundle
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.accompanist.flowlayout.FlowRow
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dictionaryapp.ui.theme.ColorModelMessage
import com.example.dictionaryapp.ui.theme.ColorUserMessage
import com.example.dictionaryapp.ui.theme.Purple80



class MainDictionaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = intent.getStringExtra("LANGUAGE") ?: "English"
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
            DictionaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        // Chuyển MainScreen sang nằm trong Scaffold
                        MainScreen(
                            language = language,
                            modifier = Modifier.padding(innerPadding), chatViewModel)

                    }
                }
            }
        }
    }
}
@Composable
fun DictionaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6)
        ),
        content = content
    )
}

@Composable
fun MainScreen(language: String, modifier: Modifier = Modifier, chatViewModel: ChatViewModel) {

    var selectedIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_11),
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedIndex) {
                0 -> DictionaryScreen(language, context = LocalContext.current)
                1 -> HistoryScreen(
                    context = LocalContext.current,
                    selectedTab = selectedIndex
                )
                2 -> ChatPage(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = chatViewModel
                )
                3 -> {
                    // Lấy dữ liệu người dùng từ SharedPreferences
                    val sharedPref = LocalContext.current.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val name = sharedPref.getString("name", "User") ?: "User"
                    val email = sharedPref.getString("email", "No email") ?: "No email"
                    val photoUrl = sharedPref.getString("photo", "") ?: ""
                    ProfileScreenContent(userName = name, userEmail = email, userPhotoUrl = photoUrl)
                }
            }
        }

        // Bottom Navigation
        BottomNavigationBar(selectedIndex) { selectedIndex = it }
    }
}

@Composable
fun BottomNavigationBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar {
        val selectedColor = Color(0xFF6200EE)
        val unselectedColor = Color.Gray

        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.img_4),
                    contentDescription = "Dictionary",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Dictionary",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) },
            colors = itemColors
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.img_14),
                    contentDescription = "History",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "History",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) },
            colors = itemColors
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.img_19),
                    contentDescription = "ChatBot",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "ChatBot",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) },
            colors = itemColors
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.img_13),
                    contentDescription = "User",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "User",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = selectedIndex == 3,
            onClick = { onItemSelected(3) },
            colors = itemColors
        )
    }
}

@Composable
fun ChatPage(modifier: Modifier = Modifier,viewModel: ChatViewModel) {
    Column(
        modifier = modifier
    ) {
        AppHeader()
        MessageList(modifier = Modifier.weight(1f),
            messageList = viewModel.messageList)
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
    }

}

@Composable
fun MessageList(modifier: Modifier = Modifier,messageList : List<MessageModel>) {
    if (messageList.isEmpty()){
        Column (
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me", fontSize = 22.sp)
        }

    }else{
        LazyColumn (
            modifier = modifier,
            reverseLayout = true
        ){
            items(messageList.reversed()){
                MessageRow(messageModel = it)
            }
        }

    }

}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role=="model"

    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        Box (
            modifier = Modifier.fillMaxWidth()
        ){
            Box (
                modifier = Modifier.align(
                    if(isModel) Alignment.BottomStart else Alignment.BottomEnd
                )
                    .padding(
                        start = if(isModel) 8.dp else 70.dp,
                        end = if(isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if(isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ){

                SelectionContainer {
                    Text(
                        text = messageModel.message,
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    )
                }


            }
        }

    }


}

@Composable
fun MessageInput(onMessageSend : (String)-> Unit) {

    var message by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = {
                message = it
            }
        )
        IconButton(onClick =    {
            if(message.isNotEmpty()){
                onMessageSend(message)
                message = ""
            }

        }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send" )
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Bot Training",
            color = Color.White,
            fontSize = 22.sp
        )
    }


}


@Composable
fun DictionaryScreen(language: String, context: Context, modifier: Modifier = Modifier) {
    val viewModel: DictionaryViewModel = viewModel()
    var searchText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    // Thêm state cho favorites
    val favorites = remember { mutableStateListOf<String>().apply { addAll(getFavorites(context)) } }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 5.dp)
        ) {
            // Phần nhập từ (giữ nguyên)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Nhập từ tiếng Anh") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,  // hoặc Icons.Default.Book
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (searchText.isNotEmpty()) {
                            saveSearchHistory(context, searchText)
                            viewModel.search(searchText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = searchText.isNotEmpty()
                ) {
                    Text("Tra Từ")
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                when {
                    viewModel.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    viewModel.error != null -> Text(
                        viewModel.error!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    viewModel.wordResult != null -> WordResult(
                        word = viewModel.wordResult!!,
                        translated = viewModel.translatedWord,
                        onAddFavorite = { word ->
                            addToFavorites(context, word)
                            // Thêm từ vào danh sách hiện tại ngay lập tức
                            if (!favorites.contains(word)) {
                                favorites.add(word)
                            }
                        }
                    )
                }

                // Truyền favorites và callback xóa từ
                FavoriteWordsList(
                    context = context,
                    favorites = favorites,
                    onWordClick = { word ->
                        viewModel.search(word)
                    },
                    onRemoveFavorite = { word ->
                        removeFromFavorites(context, word)
                        favorites.remove(word)
                    }
                )
            }
        }
    }
}

fun saveSearchHistory(context: Context, word: String) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val currentTime = timeFormat.format(Date())

    val historyItem = SearchHistory(word, currentDate, currentTime)
    HistoryManager.saveSearchHistory(context, historyItem)
}

@Composable
fun FavoriteWordsList(
    context: Context,
    favorites: List<String>,
    onWordClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "Danh sách từ đã lưu",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        favorites.forEach { word ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onWordClick(word) },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onRemoveFavorite(word)
                            }
                    )
                }
            }
        }
    }
}

fun removeFromFavorites(context: Context, word: String) {
    val favorites = getFavorites(context).toMutableList()
    favorites.remove(word)
    saveFavorites(context, favorites)
}

fun addToFavorites(context: Context, word: String) {
    val favorites = getFavorites(context).toMutableList()
    if (!favorites.contains(word)) {
        favorites.add(word)
        saveFavorites(context, favorites)
    }
}

fun getFavorites(context: Context): List<String> {
    val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    return sharedPreferences.getStringSet("favorites", emptySet())?.toList() ?: emptyList()
}

fun saveFavorites(context: Context, favorites: List<String>) {
    val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    sharedPreferences.edit().putStringSet("favorites", favorites.toSet()).apply()
}

@Composable
fun WordResult(word: DictionaryWord, translated: String?, onAddFavorite: (String) -> Unit) {
    // Initialize TextToSpeech
    var textToSpeech: TextToSpeech? = null
    val context = LocalContext.current
    val ttsInitialized = remember { mutableStateOf(false) }


    LaunchedEffect(context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val availableVoices = textToSpeech?.voices


                availableVoices?.forEach { voice ->
                    Log.d("TTS_VOICE", "Voice: ${voice.name}, Locale: ${voice.locale}")
                }


                val maleUSVoice = availableVoices?.find {
                    it.locale == Locale.US && it.name.contains("male", ignoreCase = true)
                }

                if (maleUSVoice != null) {
                    textToSpeech?.voice = maleUSVoice
                    ttsInitialized.value = true
                    Log.d("TTS", "Đã chọn voice: ${maleUSVoice.name}")
                } else {

                    val result = textToSpeech?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Không hỗ trợ US voice!")
                    } else {
                        ttsInitialized.value = true
                    }
                }
            } else {
                Log.e("TTS", "Khởi tạo TTS thất bại!")
            }
        }
    }




    fun speakWord(word: String) {
        if (ttsInitialized.value) {
            val result = textToSpeech?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Failed to speak the word.")
            }
        }
    }


    DisposableEffect(context) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Từ và bản dịch
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineSmall
            )
            translated?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Text(":", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f)) // Cái này sẽ đẩy phần tử sau nó sang bên phải

        // Icon loa để phát âm
        IconButton(
            onClick = {
                if (ttsInitialized.value) speakWord(word.word)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Phát âm"
            )
        }

        // Phát âm
        word.phonetics.firstOrNull()?.text?.let { phonetic ->
            Text(
                text = phonetic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ý nghĩa, ví dụ, synonyms và antonyms
        word.meanings.forEach { meaning ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = meaning.partOfSpeech,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    meaning.definitions.take(3).forEachIndexed { index, definition ->
                        Text(text = "${index + 1}. ${definition.definition}")

                        definition.example?.let { example ->
                            Text(
                                text = "Ví dụ: $example",
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        definition.synonyms.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.let { synonyms ->
                            FlowRowSection("Từ đồng nghĩa", synonyms)
                        }

                        definition.antonyms.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.let { antonyms ->
                            FlowRowSection("Từ trái nghĩa", antonyms)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Button(onClick = { onAddFavorite(word.word) }) {
            Text("Lưu yêu thích")


        }
    }
}



@Composable
fun FlowRowSection(title: String, words: List<String>) {
    Column {
        Text(
            text = "$title:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            words.take(10).forEach { word ->
                AssistChip(
                    onClick = { /* có thể cho click tìm lại từ này */ },
                    label = {
                        Text(text = word)
                    }
                )
            }
        }
    }
}


// Models and API

data class DictionaryWord(
    val word: String,
    val phonetics: List<Phonetic>,
    val meanings: List<Meaning>
)

data class Phonetic(val text: String?, val audio: String?)
data class Meaning(val partOfSpeech: String, val definitions: List<Definition>)
data class Definition(val definition: String, val example: String?, val synonyms: List<String>, val antonyms: List<String>)

interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWord(@Path("word") word: String): List<DictionaryWord>
}

object DictionaryApi {
    private const val BASE_URL = "https://api.dictionaryapi.dev/"

    val instance: DictionaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApiService::class.java)
    }
}

interface TranslationApiService {
    @GET("api/v1/{source}/{target}/{text}")
    suspend fun translate(
        @Path("source") source: String = "en",
        @Path("target") target: String = "vi",
        @Path("text") text: String
    ): TranslateResponse
}

data class TranslateResponse(val translation: String)

object TranslationApi {
    private const val BASE_URL = "https://lingva.ml/"

    val instance: TranslationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApiService::class.java)
    }
}

class DictionaryViewModel : ViewModel() {
    var wordResult by mutableStateOf<DictionaryWord?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var translatedWord by mutableStateOf<String?>(null)

    fun search(word: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            wordResult = null
            translatedWord = null

            try {
                val result = DictionaryApi.instance.getWord(word)
                wordResult = result.firstOrNull()
                if (wordResult == null) {
                    error = "Không tìm thấy từ"
                } else {
                    val response = TranslationApi.instance.translate(text = word)
                    translatedWord = response.translation
                }
            } catch (e: Exception) {
                error = "Lỗi: ${e.message}"
            } finally {
                isLoading = false
            }

        }
    }
}
