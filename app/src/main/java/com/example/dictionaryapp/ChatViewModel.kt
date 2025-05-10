package com.example.dictionaryapp

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content


class ChatViewModel :ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Constants.apiKey

    )

    fun sendMessage(question : String){
        Log.i("In ChatViewModel ", question)
        viewModelScope.launch {

            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role){ text(it.message) }
                    }.toList()
                )

                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing.....", "model"))

                val response = chat.sendMessage(question)
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel(response.text.toString(), "model"))
            }catch (e : Exception){
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel("Errol :" +e.message.toString(), "model"))
            }



        }
    }
}