package br.com.ucl.cryptocoin

import android.content.Context
import android.preference.PreferenceManager

object CryptoPrefs {

    const val PARAM_PUBLIC_KEY = "public_key.pref"
    const val PARAM_PRIVATE_KEY = "private_key.pref"
    const val PARAM_PRIVATE = "private.pref"

    fun setData(context: Context, publicKey: String, privateKey: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PARAM_PUBLIC_KEY, publicKey)
            .putString(PARAM_PRIVATE_KEY, privateKey)
            .apply()
    }

    fun getData(context: Context, paramKey: String): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(paramKey, null)
    }

    fun putPrivate(context: Context, pram: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PARAM_PRIVATE, pram)
                .apply()
    }

    fun updatePublicKey(context: Context, newKey: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PARAM_PUBLIC_KEY, newKey)
            .apply()
    }

}