package com.example.carte2
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.carte2.ui.theme.Carte2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Carte2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Column (modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center){
                        Carte()
                        Parametres()
                    }
                }
            }
        }
    }

    @Composable
    private fun Carte(){
        Text("ici c'est la carte enft")
    }
    @Composable
    private fun Parametres(){
        val modifier = Modifier.background(Color.Cyan)
        Text("Bienbenue dans les paramètres !")
        Row (
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Text("C'est")
            Text("Vraiment")
            Text("Un endroit cool !")
        }
    }
}