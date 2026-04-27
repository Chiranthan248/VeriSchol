package com.example.verischol.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.verischol.utils.QrUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    vcJson: String,
    onBack: () -> Unit
) {
    val qrBitmap = remember(vcJson) {
        QrUtil.generateQrCode(vcJson, 900)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credential QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Scan this QR to share credential",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(30.dp))

            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(300.dp)
            )
        }
    }
}
