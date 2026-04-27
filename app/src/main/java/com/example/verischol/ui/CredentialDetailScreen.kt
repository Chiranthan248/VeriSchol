package com.example.verischol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verischol.theme.*
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

    val cleanedJson = remember(vcJson) {
        try {
            val root = JsonParser.parseString(vcJson).asJsonObject
            if (root.has("payloadString")) root.remove("payloadString")
            gson.toJson(root)
        } catch (e: Exception) {
            vcJson
        }
    }

    val credentialData = remember(vcJson) {
        try {
            val root = JsonParser.parseString(vcJson).asJsonObject
            val payload = root.getAsJsonObject("payload")
            val iss = payload.get("iss")?.asString ?: "Unknown"
            val sub = payload.get("sub")?.asString ?: "Unknown"
            val vc = payload.getAsJsonObject("vc")
            val cs = vc.getAsJsonObject("credentialSubject")
            val n = cs.get("name")?.asString ?: "Unknown"
            val typeArray = vc.getAsJsonArray("type")
            val deg = if (typeArray != null && typeArray.size() > 1) {
                typeArray[1].asString
            } else {
                "AcademicCredential"
            }
            val iatLong = payload.get("iat")?.asLong ?: 0L
            val date = Date(iatLong)
            val iat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
            
            CredentialData(n, deg, iss, sub, iat)
        } catch (e: Exception) {
            CredentialData("Unknown", "AcademicCredential", "Unknown", "Unknown", "Unknown")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credential Record", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowQr,
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "QR")
                Spacer(Modifier.width(8.dp))
                Text("Present QR")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // VERIFIED CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (verified) SuccessGreenBackground else ErrorRedBackground
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (verified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (verified) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (verified) "On-Chain Verified" else "Verification Failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (verified) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                }
            }

            // DIGITAL CERTIFICATE CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryBlue, PrimaryLight)
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = credentialData.degreeType,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("ISSUED TO", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(credentialData.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DATE ISSUED", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(credentialData.issuedAt, fontSize = 14.sp, color = SurfaceWhite, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("ISSUER", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = credentialData.issuer,
                        fontSize = 12.sp,
                        color = SurfaceWhite,
                        lineHeight = 16.sp
                    )
                }
            }

            // METADATA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Subject DID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(credentialData.subjectDid, fontSize = 14.sp, color = TextPrimary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Raw JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = BackgroundLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cleanedJson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

data class CredentialData(
    val name: String,
    val degreeType: String,
    val issuer: String,
    val subjectDid: String,
    val issuedAt: String
)
