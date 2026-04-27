package com.example.verischol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.verischol.api.ApiClient
import com.example.verischol.api.IssueRequest
import com.example.verischol.crypto.CryptoUtil
import com.example.verischol.crypto.VerificationUtil
import com.example.verischol.data.AppDatabase
import com.example.verischol.data.Credential
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    issuerPublicKey: String,
    onCredentialSelected: (String, Boolean) -> Unit,
    onScanClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = AppDatabase.get(LocalContext.current)
    val api = ApiClient.create()
    val gson = Gson()

    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var credentialList by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // -------------------------------
        // HEADER
        // -------------------------------
        Text(
            text = "VeriSchol Wallet",
            fontSize = 30.sp,
            color = Color(0xFF1A1A1A),
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your verified academic credentials",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------------
        // REQUEST CREDENTIAL
        // -------------------------------
        ElevatedButton(
            onClick = {
                scope.launch {
                    try {
                        loading = true

                        val request = IssueRequest(
                            subject = mapOf(
                                "id" to "did:example:student1",
                                "name" to "Alice"
                            )
                        )

                        val response = api.issueVC(request)
                        val json = gson.toJson(response.vc)
                        val encrypted = CryptoUtil.encrypt(json)

                        val credential = Credential(
                            id = System.currentTimeMillis().toString(),
                            issuerDid = "did:key:$issuerPublicKey",
                            encryptedData = encrypted,
                            timestamp = System.currentTimeMillis()
                        )

                        db.credentialDao().insert(credential)
                        status = "Credential Issued Successfully"

                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Request Credential")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------
        // VIEW CREDENTIALS
        // -------------------------------
        ElevatedButton(
            onClick = {
                scope.launch {
                    loading = true
                    val list = db.credentialDao().getAll()

                    val processed = list.map { cred ->
                        val decrypted = CryptoUtil.decrypt(cred.encryptedData)
                        val map = gson.fromJson(decrypted, Map::class.java) as Map<String, *>

                        val verified = VerificationUtil.verify(
                            issuerPubKeyBase64 = issuerPublicKey,
                            credential = map
                        )

                        decrypted to verified
                    }

                    credentialList = processed
                    loading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("View Stored Credentials")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -------------------------------
        // SCAN CREDENTIAL
        // -------------------------------
        ElevatedButton(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Scan Credential")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (status.isNotEmpty()) {
            Text(
                status,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------
        // CREDENTIAL LIST UI (New Cards)
        // -------------------------------
        credentialList.forEach { (json, verified) ->

            CredentialCard(
                json = json,
                verified = verified,
                onClick = { onCredentialSelected(json, verified) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CredentialCard(
    json: String,
    verified: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(20.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                val icon = if (verified) Icons.Default.CheckCircle else Icons.Default.Error
                val color = if (verified) Color(0xFF2E7D32) else Color(0xFFC62828)

                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(34.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (verified) "Verified Credential" else "Unverified Credential",
                        fontSize = 18.sp,
                        color = color
                    )

                    Text(
                        text = "Tap to view details",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
