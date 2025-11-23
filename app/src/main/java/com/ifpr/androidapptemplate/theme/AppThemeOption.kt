package com.ifpr.androidapptemplate.theme

import androidx.annotation.StyleRes
import com.ifpr.androidapptemplate.R

enum class AppThemeOption(val remoteValue: String, @StyleRes val styleRes: Int) {
    CLASSIC("classic", R.style.Theme_AndroidAppTemplate),
    NEON("neon", R.style.Theme_AndroidAppTemplate_Neon);

    companion object {
        fun fromValue(value: String?): AppThemeOption {
            return values().firstOrNull { it.remoteValue == value } ?: CLASSIC
        }
    }
}
