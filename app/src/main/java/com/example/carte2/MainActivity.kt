package com.example.carte2

import android.annotation.SuppressLint

import android.content.pm.PackageManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.carte2.ui.theme.Carte2Theme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlin.math.round
import kotlin.math.sqrt

private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
const val distContact = 5.0
class MainActivity : ComponentActivity() {

    // Message d'affichage
    private var affiche = mutableStateOf("trkl")
    // User input
    private var entr  = mutableStateOf("")
    private var chrono = mutableStateOf(0.0)
    // Indice dans le parcours
    private var ind = mutableStateOf(0)
    private var team = mutableStateOf("PAS SELECTED")
    // Questions et réponses
    private var q = mutableStateOf("")
    private var r = mutableStateOf("")

    private lateinit var qandr:Array<String>
    private lateinit var long:IntArray
    private lateinit var latt:IntArray
    private lateinit var indices:Array<String>
    // parcours Lupercule, Vazogo, Tiposac
    private lateinit var pLu:IntArray
    private lateinit var pVa:IntArray
    private lateinit var pTi:IntArray

    // current position
    private var cuLong = mutableStateOf(0.0)
    private var cuLatt = mutableStateOf(0.0)
    private var dist = mutableStateOf(1000.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val actv = this
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            Carte2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    LaunchedEffect(key1 = Unit, block = {
                        val distance by actv.dist
                        while (true) {
                            posi()
                            delay(100)
                            if (actv.chrono.value > 0.0) {
                                actv.chrono.value -= 0.1
                                actv.entr.value = "Attendre encore ${round3(actv.chrono.value)}s"
                            }
                            if(distance > distContact){
                                actv.q.value = ""
                            }else{
                                updateQandR()
                            }
                        }
                    })
                    actv.qandr = stringArrayResource(R.array.qandr)
                    actv.indices = stringArrayResource(R.array.indices)
                    actv.qandr.shuffle()
                    actv.long = integerArrayResource(R.array.longitude)
                    actv.latt = integerArrayResource(R.array.latitude)
                    actv.pLu = integerArrayResource(R.array.parcoursLupercule)
                    actv.pVa = integerArrayResource(R.array.parcoursVazogo)
                    actv.pTi = integerArrayResource(R.array.parcoursTiposac)

                    RequestLocationPermission(
                        onPermissionGranted = { actv.affiche.value = "All good ! :D";},
                        onPermissionDenied = { actv.affiche.value = "sadee" },
                        onPermissionsRevoked = {
                            actv.affiche.value = "revoked wtf"
                            posi()
                        })

                    Column (modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center){
                        ChooseTeam()
                        ErrorMessg()
                        // Actual planned UI
                        Etape()
                        Parcours()
                        Proche()
                    }
                }
            }
        }
    }

    // Update the values of the question and answer
    private fun updateQandR(){
        val current = this.qandr[this.ind.value]
        val sep = current.indexOf(";")
        this.q.value = current.substring(0,sep)
        this.r.value = current.substring(sep+1,current.length)
    }

    private fun posi(){
        getCurrentLocation(
            onGetCurrentLocationSuccess = {
                pos : Pair<Double,Double> ->
                this.cuLong.value = pos.second
                this.cuLatt.value = pos.first },
            onGetCurrentLocationFailed = {err : Exception ->
                this.affiche.value = err.toString()})
    }


    @Composable
    private fun ChooseTeam(){
        val actv  = this
        val modifier = Modifier
        Text("Choix de l'équipe : ")
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(onClick = { actv.team.value = "Vazogo" }) {
                Text("Vazogo",modifier = modifier)
            }
            Button(onClick = { actv.team.value = "Lupercule" }) {
                Text("Lupercule",modifier = modifier)
            }
            Button(onClick = { actv.team.value = "Tiposac" }) {
                Text("Tiposac",modifier = modifier)
            }
        }
    }

    @Composable
    private fun Parcours(){
        val parcours:IntArray = when (this.team.value) {
            "Vazogo" -> { this.pVa }
            "Lupercule" -> { this.pLu }
            "Tiposac" -> { this.pTi }
            else -> intArrayOf(6, 2)
        }

        val ind by this.ind
        val long by this.cuLong
        val latt by this.cuLatt

        val longDest = 5.98 + this.long[parcours[ind]-1] * 0.0000001
        val lattDest = 43.1 + this.latt[parcours[ind]-1] * 0.0000001
        val deltaLong = round3((longDest - long)*10000.0)
        val deltaLatt = round3((lattDest - latt)*10000.0)
        this.dist.value = round3(sqrt(deltaLong*deltaLong + deltaLatt*deltaLatt))

        Text("vers le Nord de : $deltaLatt deg \nvers l'Est de : $deltaLong deg \nDistance : ${this.dist.value} µ-metron")
        Text("Indice du lieu : ${this.indices[parcours[ind]-1]}")
    }

    @Composable
    private fun Etape(){
        val ind by this.ind
        val equip by this.team
        Text("Bravo pour la $ind étape $equip ! :D",
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
    @Preview
    @Composable
    private fun Proche(modifier : Modifier = Modifier){
        val quest by this.q
        val resp by this.r
        val dist by this.dist
        Text(quest, modifier = modifier.background(
            color = androidx.compose.ui.graphics.Color.LightGray
        ))
        val actv = this
        Row(
            modifier = Modifier.fillMaxWidth().background(
                color = androidx.compose.ui.graphics.Color.LightGray),
            verticalAlignment = Alignment.CenterVertically
        )
            {
            TextField(
                value = actv.entr.value,
                onValueChange = { newText: String -> actv.entr.value = newText })
            Button(onClick = {
                if (actv.entr.value.lowercase() == resp && dist < distContact) {
                    actv.ind.value += 1
                } else {
                    actv.entr.value = "Non."
                    if(resp.length == 1){
                        actv.chrono.value = 60.0
                    }
                }
            }) {
                Text("Vérifier", modifier = modifier)
            }
        }
    }

    @Composable
    private fun ErrorMessg(modifier : Modifier = Modifier){
        val logs by this.affiche
        Text(logs, modifier = modifier)
    }


    private fun areLocationPermissionsGranted(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                )
    }

    /**
     * Retrieves the current user location asynchronously.
     *
     * @param onGetCurrentLocationSuccess Callback function invoked when the current location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetCurrentLocationFailed Callback function invoked when an error occurs while retrieving the current location.
     *        It provides the Exception that occurred.
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     */

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess:  (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed:  (Exception) -> Unit,
        priority: Boolean = true
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }

}

fun round3(x:Double): Double{
    return round(x*1000.0)/1000.0
}


/**
 * Composable function to request location permissions and handle different scenarios.
 * @param onPermissionGranted Callback to be executed when all requested permissions are granted.
 * @param onPermissionDenied Callback to be executed when any requested permission is denied.
 * @param onPermissionsRevoked Callback to be executed when previously granted permissions are revoked.
 */

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionsRevoked: () -> Unit
) {
    // Initialize the state for managing multiple location permissions.
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )
    // Use LaunchedEffect to handle permissions logic when the composition is launched.
    LaunchedEffect(key1 = permissionState) {
        // Check if all previously granted permissions are revoked.
        val allPermissionsRevoked = permissionState.permissions.size == permissionState.revokedPermissions.size

        // Filter permissions that need to be requested.
        val permissionsToRequest = permissionState.permissions.filter {
            !it.status.isGranted
        }

        // If there are permissions to request, launch the permission request.
        if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

        // Execute callbacks based on permission status.
        if (allPermissionsRevoked) {
            onPermissionsRevoked()
        } else {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}