package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF18272E)
private val MutedInk = Color(0xFF607078)
private val Canvas = Color(0xFFF7F8F5)
private val Accent = Color(0xFF0D766E)
private val AccentSoft = Color(0xFFDDF3F0)

@Composable
@Preview
fun App() {
    var receiptText by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
            ) {
                Text("Warranty Radar", color = Accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(28.dp))
                Text("Add a receipt", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Paste the details or upload a photo. We’ll help you keep track of the warranty.",
                    color = MutedInk,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(28.dp))

                Text("Receipt text", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = receiptText,
                    onValueChange = { receiptText = it },
                    modifier = Modifier.fillMaxWidth().height(132.dp),
                    placeholder = { Text("Paste receipt details here…") },
                    supportingText = { Text("Merchant, items, purchase date, and price are helpful.") },
                    shape = RoundedCornerShape(14.dp),
                )

                Spacer(Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(Modifier.weight(1f).height(1.dp).background(Color(0xFFDDE3E1)))
                    Text("OR", color = MutedInk, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f).height(1.dp).background(Color(0xFFDDE3E1)))
                }
                Spacer(Modifier.height(28.dp))

                ReceiptUploadPlaceholder()
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Save receipt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptUploadPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF9BCFC9), RoundedCornerShape(16.dp))
            .background(AccentSoft)
            .padding(vertical = 26.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text("↑", color = Accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Upload receipt image", color = Ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap to choose a photo or take one with your camera",
            color = MutedInk,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Text("JPG, PNG or PDF · up to 10 MB", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
