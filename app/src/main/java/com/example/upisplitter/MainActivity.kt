package com.example.upisplitter

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.upisplitter.data.local.AppDatabase
import com.example.upisplitter.data.repository.TransactionRepository
import com.example.upisplitter.navigation.NavGraph
import com.example.upisplitter.ui.theme.UPISplitterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(Color.BLACK)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.transactionDao(), database.upiIdDao())

        setContent {
            UPISplitterTheme {
                NavGraph(repository = repository)
            }
        }
    }
}
