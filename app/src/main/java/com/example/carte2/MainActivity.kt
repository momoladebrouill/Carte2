package com.example.carte2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.carte2.ui.theme.Carte2Theme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource


private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

class MainActivity : ComponentActivity() {

    private lateinit var affiche: MutableState<String>
    private lateinit var entr: MutableState<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        this.affiche = mutableStateOf("ee");
        this.entr = mutableStateOf("cc bg")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            Carte2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        contenu()
                        Hello()
                        Bouton()
                        Entree()
                    }

                }
            }
        }
    }
    @Composable
    private fun Bouton(){
        Button(onClick = { this.affiche.value = "eknosian";posi()}) {
            Text(text = "CLIQUE ALLEZ CLIQUE")
        }
    }


    private fun posi(){
        getCurrentLocation(onGetCurrentLocationSuccess = {input : Pair<Double,Double> -> showLoc(input)}, onGetCurrentLocationFailed = {err : Exception -> showFail(err)});
    }
    @Composable
    private fun contenu(){
        RequestLocationPermission(
            onPermissionGranted = { this.affiche.value = "yipee";},
            onPermissionDenied = { this.affiche.value = "sadee" },
            onPermissionsRevoked = {
                this.affiche.value = "revoked wtf";
                posi()
                });

    }

    private fun showFail(err : Exception) {
        this.affiche.value = err.toString()
    }

    @Composable
    fun Entree(){
        TextField(value = this.entr.value, onValueChange = {newText : String -> this.entr.value = newText})
    }

    private fun showLoc(pos : Pair<Double,Double>){
        this.affiche.value = pos.toString()
    }
    @Composable
    private fun Hello(modifier : Modifier = Modifier){
        val logs by this.affiche
        Text(logs, modifier = modifier.padding(all = 15.dp))
    }


    /**
     * Checks if location permissions are granted.
     *
     * @return true if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted; false otherwise.
     */
    private fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
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

/**
 * Composable function to request location permissions and handle different scenarios.
 *
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
        val allPermissionsRevoked =
            permissionState.permissions.size == permissionState.revokedPermissions.size

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