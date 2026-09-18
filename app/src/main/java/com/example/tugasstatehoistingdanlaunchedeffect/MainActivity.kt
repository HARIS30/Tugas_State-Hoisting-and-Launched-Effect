package com.example.tugasstatehoistingdanlaunchedeffect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicketScreenParent()
                }
            }
        }
    }
}

// Model Status UI
sealed class BookingStatus {
    data object Idle : BookingStatus()
    data object EmptyName : BookingStatus()
    data object Processing : BookingStatus()
    data object Success : BookingStatus()
}

// 1. PARENT COMPOSABLE (Mengelola State / State Hoisting)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreenParent() {
    // 3 State yang dikelola Parent sesuai ketentuan soal:
    var namaPembeli by remember { mutableStateOf("") }
    var jumlahTiket by remember { mutableIntStateOf(1) }
    var hargaTiket by remember { mutableIntStateOf(50000) }

    // State pendukung alur proses
    var isProcessing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<BookingStatus>(BookingStatus.Idle) }

    // LaunchedEffect dengan trigger perubahan nilai isProcessing
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            status = BookingStatus.Processing
            // Delay 5 detik sesuai ketentuan deskripsi soal
            delay(5000)
            status = BookingStatus.Success
            isProcessing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pemesanan Tiket", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        TicketScreenContent(
            modifier = Modifier.padding(paddingValues),
            namaPembeli = namaPembeli,
            onNamaChange = { namaPembeli = it },
            jumlahTiket = jumlahTiket,
            onIncrementTiket = { jumlahTiket++ },
            onDecrementTiket = { if (jumlahTiket > 1) jumlahTiket-- },
            hargaTiket = hargaTiket,
            totalHarga = hargaTiket * jumlahTiket,
            status = status,
            isProcessing = isProcessing,
            onPesanClick = {
                if (namaPembeli.trim().isEmpty()) {
                    status = BookingStatus.EmptyName
                } else {
                    isProcessing = true
                }
            }
        )
    }
}

// 2. STATELESS CHILD COMPOSABLE
@Composable
fun TicketScreenContent(
    modifier: Modifier = Modifier,
    namaPembeli: String,
    onNamaChange: (String) -> Unit,
    jumlahTiket: Int,
    onIncrementTiket: () -> Unit,
    onDecrementTiket: () -> Unit,
    hargaTiket: Int,
    totalHarga: Int,
    status: BookingStatus,
    isProcessing: Boolean,
    onPesanClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input Nama
        Text("Nama", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = namaPembeli,
            onValueChange = onNamaChange,
            placeholder = { Text("Masukkan nama Anda") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isProcessing
        )

        // Informasi Harga
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Harga per Tiket: Rp $hargaTiket", color = Color.Gray, fontSize = 13.sp)
            Text("Total: Rp $totalHarga", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        // Counter Jumlah Tiket
        Text("Jumlah Tiket", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDecrementTiket,
                enabled = !isProcessing && jumlahTiket > 1,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = jumlahTiket.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = onIncrementTiket,
                enabled = !isProcessing,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tombol Pesan Tiket
        Button(
            onClick = onPesanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pesan Tiket", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kotak Status
        StatusCard(status = status)
    }
}

// 3. STATUS CARD COMPONENT
@Composable
fun StatusCard(status: BookingStatus) {
    val (backgroundColor, contentColor) = when (status) {
        BookingStatus.Idle -> Color(0xFFF5F5F5) to Color.DarkGray
        BookingStatus.Processing -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        BookingStatus.Success -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        BookingStatus.EmptyName -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (status) {
                BookingStatus.Idle -> {
                    Text(
                        text = "Status: Silakan pesan tiket",
                        color = contentColor,
                        fontSize = 13.sp
                    )
                }
                BookingStatus.Processing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = contentColor
                    )
                    Text(
                        text = "Status: Memproses pesanan...",
                        color = contentColor,
                        fontSize = 13.sp
                    )
                }
                BookingStatus.Success -> {
                    Text(
                        text = "✓",
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Status: Tiket telah dipesan",
                        color = contentColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
                BookingStatus.EmptyName -> {
                    Text(
                        text = "✕",
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Status: Nama Masih Kosong",
                        color = contentColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}