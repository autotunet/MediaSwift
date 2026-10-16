package com.example.ui.i18n

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val englishName: String,
    val flag: String
) {
    SPANISH("es", "Español", "Spanish", "🇪🇸"),
    ENGLISH("en", "English", "English", "🇺🇸"),
    PORTUGUESE("pt", "Português", "Portuguese", "🇧🇷"),
    FRENCH("fr", "Français", "French", "🇫🇷");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SPANISH
        }
    }
}
