package com.example.verischol.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.verischol.api.ApiClient
import com.example.verischol.api.IssueRequest
import com.example.verischol.crypto.CryptoUtil
import com.example.verischol.crypto.VerificationUtil
import com.example.verischol.data.AppDatabase
import com.example.verischol.data.Credential
import com.example.verischol.theme.*
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
    var credentialList by remember { mutableStateOf(listOf<Triple<String, String, Boolean>>()) }

    // Load credentials on init
    val refreshList = {
        scope.launch {
            val list = db.credentialDao().getAll()
            val processed = list.mapNotNull { cred ->
                try {
                    val decrypted = CryptoUtil.decrypt(cred.encryptedData)
                    val map = gson.fromJson(decrypted, Map::class.java) as Map<String, *>
                    val verified = VerificationUtil.verify(issuerPublicKey, map)
                    Triple(cred.id, decrypted, verified)
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Failed to decrypt/parse credential ${cred.id}", e)
                    null
                }
            }
            credentialList = processed
        }
    }

    LaunchedEffect(Unit) {
        refreshList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryLight)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "VeriSchol Wallet",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScanClick,
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan QR")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Welcome Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryBlue, PrimaryLight)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Total Credentials",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${credentialList.size}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    loading = true
                                    status = "Requesting..."
                                    val request = IssueRequest(
                                        subject = mapOf(
                                            "id" to "did:example:student1",
                                            "name" to "Alice Student"
                                        )
                                    )
                                    // MOCK BACKEND FALLBACK FOR END-TO-END DEMO
                                    var vcJson = ""
                                    try {
                                        val response = api.issueVC(request)
                                        vcJson = gson.toJson(response.vc)
                                        status = "Credential Issued via API"
                                    } catch (e: Exception) {
                                        // Inline mocked signed VC if API fails (offline mode)
                                        status = "API Offline. Generating Local Mock..."
                                        
                                        val unsigned = mapOf(
                                            "iss" to "did:key:$issuerPublicKey",
                                            "sub" to request.subject["id"],
                                            "vc" to mapOf(
                                                "@context" to listOf("https://www.w3.org/2018/credentials/v1"),
                                                "type" to listOf("VerifiableCredential", "AcademicCredential"),
                                                "credentialSubject" to request.subject
                                            ),
                                            "iat" to System.currentTimeMillis()
                                        )
                                        val payloadString = gson.toJson(unsigned)
                                        
                                        // In a real app, we can't sign locally without the private key.
                                        // But for this demo, we'll simulate a signed structure.
                                        vcJson = gson.toJson(mapOf(
                                            "payload" to unsigned,
                                            "payloadString" to payloadString,
                                            "signature" to "MOCK_SIGNATURE_FOR_DEMO" 
                                        ))
                                    }

                                    val encrypted = CryptoUtil.encrypt(vcJson)
                                    val credential = Credential(
                                        id = System.currentTimeMillis().toString(),
                                        issuerDid = "did:key:$issuerPublicKey",
                                        encryptedData = encrypted,
                                        timestamp = System.currentTimeMillis()
                                    )

                                    db.credentialDao().insert(credential)
                                    status = "Credential Issued Successfully"

                                    // Refresh list
                                    refreshList()

                                } catch (e: Exception) {
                                    status = "Error: ${e.message}"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Demo Detail", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryBlue)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (status.isNotEmpty()) {
                Text(status, color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text("Your Credentials", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            if (credentialList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No credentials saved yet.", color = TextSecondary)
                }
            } else {
                credentialList.forEach { (id, json, verified) ->
                    CredentialCard(
                        json = json,
                        verified = verified,
                        onClick = { onCredentialSelected(json, verified) },
                        onDelete = {
                            scope.launch {
                                db.credentialDao().deleteById(id)
                                refreshList()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // padding for fab
        }
    }
}

@Composable
fun CredentialCard(
    json: String,
    verified: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (verified) SuccessGreenBackground else ErrorRedBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (verified) Icons.Default.VerifiedUser else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (verified) SuccessGreen else ErrorRed
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Academic Credential",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (verified) "Blockchain Verified" else "Verification Warning",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
            }
        }
    }
}
