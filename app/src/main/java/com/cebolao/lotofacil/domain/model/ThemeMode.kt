package com.cebolao.lotofacil.domain.model

enum class ThemeMode(val storageValue: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return when (value?.lowercase()) {
                LIGHT.storageValue -> LIGHT
                DARK.storageValue -> DARK
                SYSTEM.storageValue -> SYSTEM
                else -> SYSTEM
            }
        }
    }
}
