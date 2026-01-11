package com.example.lab56_testowanie

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANG = "lang" // "pl" albo "en"

    fun applySavedLanguage(context: Context) {
        val saved = getSavedLanguage(context) ?: return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
    }

    fun toggleLanguage(context: Context) {
        val current = getCurrentLanguage(context)
        val newLang = if (current == "pl") "en" else "pl"
        saveLanguage(context, newLang)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLang))
    }

    fun getCurrentLanguage(context: Context): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        val langFromAppCompat = if (!locales.isEmpty) locales[0]?.language else null
        return (langFromAppCompat ?: Locale.getDefault().language).lowercase()
    }

    private fun getSavedLanguage(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)
    }

    private fun saveLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang)
            .apply()
    }
}
