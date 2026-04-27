package com.example.verischol

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.verischol.ui.CredentialDetailScreen
import com.example.verischol.ui.HomeScreen
import com.example.verischol.ui.QrScreen
import com.example.verischol.ui.ScanScreen
import com.example.verischol.crypto.VerificationUtil
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    private val issuerPublicKey =
        "iJZ2TuLIQKTXDnoeZ4dell7gqSY8uipXTFdhk5VwwUU="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gson = Gson()

        setContent {

            val navController = rememberNavController()

            MaterialTheme {

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    // -------------------------
                    // HOME SCREEN
                    // -------------------------
                    composable("home") {
                        HomeScreen(
                            issuerPublicKey = issuerPublicKey,
                            onCredentialSelected = { json, verified ->
                                val encoded = Uri.encode(json)
                                navController.navigate("detail/$encoded/$verified")
                            },
                            onScanClick = {
                                navController.navigate("scan")
                            }
                        )
                    }

                    // -------------------------
                    // DETAILS SCREEN
                    // -------------------------
                    composable(
                        route = "detail/{json}/{verified}",
                        arguments = listOf(
                            navArgument("json") { type = NavType.StringType },
                            navArgument("verified") { type = NavType.BoolType }
                        )
                    ) { backStack ->

                        val json = backStack.arguments?.getString("json") ?: "{}"
                        val verified = backStack.arguments?.getBoolean("verified") ?: false

                        CredentialDetailScreen(
                            vcJson = json,
                            verified = verified,
                            onBack = { navController.popBackStack() },
                            onShowQr = {
                                val encoded = Uri.encode(json)
                                navController.navigate("qr/$encoded")
                            }
                        )
                    }

                    // -------------------------
                    // QR SCREEN
                    // -------------------------
                    composable(
                        route = "qr/{vcJson}",
                        arguments = listOf(
                            navArgument("vcJson") { type = NavType.StringType }
                        )
                    ) { backStack ->

                        val vcJson = backStack.arguments?.getString("vcJson") ?: ""

                        QrScreen(
                            vcJson = vcJson,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // -------------------------
                    // SCANNER SCREEN (fixed)
                    // -------------------------
                    composable("scan") {
                        ScanScreen(
                            onBack = { navController.popBackStack() },
                            onScanned = { scannedJson ->

                                // parse scanned JSON into map (VerificationUtil expects Map<String, *>)
                                val map = try {
                                    gson.fromJson(scannedJson, Map::class.java) as Map<String, *>
                                } catch (e: Exception) {
                                    // If parsing fails, treat as not verified and still navigate
                                    null
                                }

                                val verified = if (map != null) {
                                    // call your VerificationUtil (returns Boolean)
                                    VerificationUtil.verify(
                                        issuerPubKeyBase64 = issuerPublicKey,
                                        credential = map
                                    )
                                } else {
                                    false
                                }

                                val encoded = Uri.encode(scannedJson)
                                navController.navigate("detail/$encoded/$verified")
                            }
                        )
                    }
                }
            }
        }
    }
}
