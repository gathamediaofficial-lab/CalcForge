package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CalcDatabase
import com.example.data.HistoryRepository
import com.example.data.UserPreferencesRepository
import com.example.ui.CalcForgeApp
import com.example.ui.theme.CalcForgeTheme
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.ToolsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val database = CalcDatabase.getInstance(applicationContext)
        val historyRepository = HistoryRepository(database.calculationHistoryDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(CalculatorViewModel::class.java) ->
                        CalculatorViewModel(historyRepository, preferencesRepository) as T
                    modelClass.isAssignableFrom(ConverterViewModel::class.java) ->
                        ConverterViewModel() as T
                    modelClass.isAssignableFrom(ToolsViewModel::class.java) ->
                        ToolsViewModel() as T
                    modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                        HistoryViewModel(historyRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }

        setContent {
            val calculatorViewModel: CalculatorViewModel = viewModel(factory = viewModelFactory)
            val converterViewModel: ConverterViewModel = viewModel(factory = viewModelFactory)
            val toolsViewModel: ToolsViewModel = viewModel(factory = viewModelFactory)
            val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)

            val calcState by calculatorViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(calcState.isDarkMode) {
                val barStyle = if (calcState.isDarkMode) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
            }

            CalcForgeTheme(darkTheme = calcState.isDarkMode) {
                CalcForgeApp(
                    calculatorViewModel = calculatorViewModel,
                    converterViewModel = converterViewModel,
                    toolsViewModel = toolsViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}
