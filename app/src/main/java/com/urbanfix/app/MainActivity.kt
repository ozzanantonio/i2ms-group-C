package com.urbanfix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.urbanfix.app.ui.UrbanFixApp
import com.urbanfix.app.ui.theme.UrbanFixTheme
import com.urbanfix.app.viewmodel.UrbanFixViewModel

class MainActivity : ComponentActivity() {
    private val vm: UrbanFixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark = vm.darkOverride ?: isSystemInDarkTheme()
            UrbanFixTheme(dark, vm.largeText) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UrbanFixApp(vm, dark)
                }
            }
        }
    }
}
