package com.example.dictionaryapp

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

// Lớp lưu trữ lịch sử tìm kiếm
data class SearchHistory(val word: String, val date: String, val time: String)

// Quản lý lịch sử tìm kiếm
object HistoryManager {
    private const val PREFS_NAME = "search_history"
    private const val HISTORY_KEY = "history_list"

    fun saveSearchHistory(context: Context, historyItem: SearchHistory) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = sharedPreferences.getString(HISTORY_KEY, "[]")
        val historyListType = object : TypeToken<MutableList<SearchHistory>>() {}.type
        val historyList: MutableList<SearchHistory> = Gson().fromJson(historyJson, historyListType)

        historyList.add(historyItem)
        val updatedHistoryJson = Gson().toJson(historyList)
        sharedPreferences.edit().putString(HISTORY_KEY, updatedHistoryJson).apply()
    }

    fun getSearchHistory(context: Context): List<SearchHistory> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = sharedPreferences.getString(HISTORY_KEY, "[]")
        val historyListType = object : TypeToken<MutableList<SearchHistory>>() {}.type
        val historyList: MutableList<SearchHistory> = Gson().fromJson(historyJson, historyListType)

        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return historyList.sortedByDescending { dateTimeFormat.parse("${it.date} ${it.time}") }
    }

    fun clearSearchHistory(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(HISTORY_KEY).apply()
    }

    fun deleteSelectedHistory(context: Context, selectedItems: List<SearchHistory>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = sharedPreferences.getString(HISTORY_KEY, "[]")
        val historyListType = object : TypeToken<MutableList<SearchHistory>>() {}.type
        val historyList: MutableList<SearchHistory> = Gson().fromJson(historyJson, historyListType)

        val selectedWords = selectedItems.map { it.word }.toSet()
        val updatedList = historyList.filterNot { it.word in selectedWords }

        val updatedHistoryJson = Gson().toJson(updatedList)
        sharedPreferences.edit().putString(HISTORY_KEY, updatedHistoryJson).apply()
    }
}

@Composable
fun HistoryScreen(context: Context, selectedTab: Int) {
    var historyList by remember { mutableStateOf(emptyList<SearchHistory>()) }
    var selectedItems by remember { mutableStateOf(setOf<SearchHistory>()) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredHistoryList by remember { mutableStateOf(emptyList<SearchHistory>()) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            historyList = HistoryManager.getSearchHistory(context)
            filteredHistoryList = historyList
        }
    }

    fun filterHistory(query: String) {
        filteredHistoryList = if (query.isEmpty()) {
            historyList
        } else {
            historyList.filter { it.word.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lịch sử tra cứu",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
            )
            IconButton(
                onClick = {
                    HistoryManager.deleteSelectedHistory(context, selectedItems.toList())
                    historyList = HistoryManager.getSearchHistory(context)
                    filteredHistoryList = if (searchQuery.isEmpty()) {
                        historyList
                    } else {
                        historyList.filter { it.word.contains(searchQuery, ignoreCase = true) }
                    }
                    selectedItems = emptySet()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.img_12),
                    contentDescription = "Delete Selected",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filterHistory(it)
            },
            label = { Text("Tìm kiếm trong lịch sử") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredHistoryList) { historyItem ->
                val isSelected = selectedItems.contains(historyItem)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            selectedItems = if (isSelected) selectedItems - historyItem else selectedItems + historyItem
                        },
                    elevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedItems = if (checked) selectedItems + historyItem else selectedItems - historyItem
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = historyItem.word,
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "${historyItem.date} ${historyItem.time}",
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(context: Context) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Tìm kiếm từ") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (query.isNotEmpty()) {
                    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val historyItem = SearchHistory(word = query, date = currentDate, time = currentTime)
                    HistoryManager.saveSearchHistory(context, historyItem)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Tìm kiếm")
        }
    }
}
