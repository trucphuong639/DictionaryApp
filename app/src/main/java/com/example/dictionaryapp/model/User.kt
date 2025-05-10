package com.example.dictionaryapp.model

import com.google.gson.Gson

data class User(
    var name: String? = null,
    var email: String? = null,
    var pictureUrl: String? = null
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}