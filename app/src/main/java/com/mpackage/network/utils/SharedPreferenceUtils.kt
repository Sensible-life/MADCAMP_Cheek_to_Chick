package com.mpackage.network.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {

    private const val PREFS_NAME = "AppPreferences"

    // userId 저장
    fun saveUserId(context: Context, userId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("USER_ID", userId).apply()
    }

    // userId 불러오기
    fun getUserId(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    // userId 삭제 (로그아웃 시 호출)
    fun clearUserId(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("USER_ID").apply()
    }
}
