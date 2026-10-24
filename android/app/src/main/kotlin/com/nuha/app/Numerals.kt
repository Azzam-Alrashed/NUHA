package com.nuha.app

import androidx.compose.runtime.staticCompositionLocalOf

/** How numbers are written on screen; a per-child setting (FR-SET-002) until OI-02 settles one system for everyone. */
enum class Numerals(val id: String) {
    Western("western"),
    ArabicIndic("arabic-indic");

    fun format(n: Int): String = when (this) {
        Western -> n.toString()
        ArabicIndic -> n.toString().map { if (it in '0'..'9') '٠' + (it - '0') else it }.joinToString("")
    }

    companion object {
        fun of(id: String) = entries.firstOrNull { it.id == id } ?: Pending.DefaultNumerals
    }
}

/** The numerals of whoever is using the screen: the playing child's setting in child mode, else the default. */
val LocalNumerals = staticCompositionLocalOf { Pending.DefaultNumerals }
