package com.ev.iot2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.ev.iot2.data.database.DatabaseHelper
import com.ev.iot2.ui.navigation.IoTempNavHost
import com.ev.iot2.ui.theme.IOT2Theme

class MainActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        databaseHelper = DatabaseHelper(this)
        
        setContent {
            IOT2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController = rememberNavController()
                    IoTempNavHost(
                        navController = navController,
                        databaseHelper = databaseHelper
                    )
                }
            }
        }
    }
}