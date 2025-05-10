package com.example.dictionaryapp.prefs

import android.content.Context

class MySharedPreferences(private val mContext: Context) {

    companion object {
        private const val APP_SAVE_PREFERENCES = "app_save_preferences"
    }

    private fun getSharedPreferences() =
        mContext.getSharedPreferences(APP_SAVE_PREFERENCES, Context.MODE_PRIVATE)


    fun remove(key: String) {
        getSharedPreferences().edit().remove(key).apply()
    }
    fun putLongValue(key: String, n: Long) {
        getSharedPreferences().edit().putLong(key, n).apply()
    }

    fun getLongValue(key: String): Long {
        return getSharedPreferences().getLong(key, 0L)
    }

    fun putIntValue(key: String, n: Int) {
        getSharedPreferences().edit().putInt(key, n).apply()
    }

    fun getIntValue(key: String): Int {
        return getSharedPreferences().getInt(key, 0)
    }

    fun putStringValue(key: String, s: String) {
        getSharedPreferences().edit().putString(key, s).apply()
    }

    fun getStringValue(key: String): String {
        return getSharedPreferences().getString(key, "") ?: ""
    }

    fun getStringValue(key: String, defaultValue: String): String {
        return getSharedPreferences().getString(key, defaultValue) ?: defaultValue
    }

    fun putBooleanValue(key: String, b: Boolean) {
        getSharedPreferences().edit().putBoolean(key, b).apply()
    }

    fun getBooleanValue(key: String): Boolean {
        return getSharedPreferences().getBoolean(key, false)
    }

    fun putFloatValue(key: String, f: Float) {
        getSharedPreferences().edit().putFloat(key, f).apply()
    }

    fun getFloatValue(key: String): Float {
        return getSharedPreferences().getFloat(key, 0.0f)
    }
}
