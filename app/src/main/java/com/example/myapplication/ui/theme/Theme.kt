package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GrowthColorScheme = lightColorScheme(
    primary = GrowthPurplePrimary,
    secondary = GrowthPurpleDrawer,
    tertiary = GrowthGreenAction,
    background = Color.White,
    surface = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GrowthColorScheme,
        typography = Typography,
        content = content
    )
}
