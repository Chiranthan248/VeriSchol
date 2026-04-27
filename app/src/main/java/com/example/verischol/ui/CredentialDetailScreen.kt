package com.example.verischol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
    vcJson: String,
    verified: Boolean,
    onBack: () -> Unit,
    onShowQr: () -> Unit
) {
    val gson = remember { GsonBuilder().setPrettyPrinting().create() }

    // Clean JSON (remove payloadString if exists)
    val cleanedJson = remember(vcJson) {
        try {
            val root = JsonParser.parseString(vcJson).asJsonObject
            if (root.has("payloadString")) root.remove("payloadString")
            gson.toJson(root)
        } catch (e: Exception) {
            vcJson
        }
    }

    // Extract structured fields
    val root = JsonParser.parseString(vcJson).asJsonObject
    val payload = root.getAsJsonObject("payload")

    val issuer = payload.get("iss")?.asString ?: "Unknown"
    val subjectDid = payload.get("sub")?.asString ?: "Unknown"

    val vc = payload.getAsJsonObject("vc")
    val cs = vc.getAsJsonObject("credentialSubject")

    val name = cs.get("name")?.asString ?: "Unknown"

    // FIXED type extraction
    val typeArray = vc.getAsJsonArray("type")
    val degreeType = if (typeArray != null && typeArray.size() > 1)
        typeArray[1].asString
    else
        "DegreeCredential"

    // Format date
    val issuedAt =
        try {
            val date = Date(payload.get("iat").asLong)
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            "Unknown"
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credential Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // VERIFIED CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (verified) Color(0xFFDFF6E0)
                        else Color(0xFFFFE1E1)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (verified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (verified) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (verified) "Credential Verified" else "Verification Failed",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // DEGREE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text = "🎓 Academic Credential",
                        fontSize = 22.sp,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Name: $name", fontSize = 16.sp)
                    Text("Degree Type: $degreeType", fontSize = 16.sp)
                    Text("Subject DID: $subjectDid", fontSize = 15.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Issued On:", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text(issuedAt, fontSize = 15.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Issuer:", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text(issuer, fontSize = 15.sp, color = Color.Gray)
                }
            }

            // QR BUTTON
            Button(
                onClick = onShowQr,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Show QR Code")
            }

            // JSON VIEWER
           /* Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Full Credential JSON", fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(cleanedJson, fontSize = 13.sp)
                }
            }*/
        }
    }
}
