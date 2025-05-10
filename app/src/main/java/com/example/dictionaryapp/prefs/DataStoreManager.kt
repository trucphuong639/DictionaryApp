package com.example.dictionaryapp.prefs

import android.content.Context
import com.example.dictionaryapp.model.User
import com.google.gson.Gson

object DataStoreManager {

    private const val PREF_USER_INFO = "pref_user_info"
    private const val POSITION_ADDRESS = "position_address"

    private var sharedPreferences: MySharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = MySharedPreferences(context)
        }
    }

    fun setUser(user: User?) {
        val jsonUser = user?.toJson() ?: ""
        sharedPreferences?.putStringValue(PREF_USER_INFO, jsonUser)
    }
    fun clearUser() {
        sharedPreferences?.remove(PREF_USER_INFO)  // Xóa thông tin người dùng
    }
    fun getUser(): User {
        val jsonUser = sharedPreferences?.getStringValue(PREF_USER_INFO)
        return if (!StringUtil.isEmpty(jsonUser)) {
            Gson().fromJson(jsonUser, User::class.java)
        } else {
            User()
        }
    }

}
