package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )
    setContent {
      MyApplicationTheme {
        val gameViewModel: GameViewModel = viewModel()
        MainGameScreen(viewModel = gameViewModel)
      }
    }
  }
}
