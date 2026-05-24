package com.example.campusfreshzone.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable

@Composable
fun MainScreen() {

    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "현재 상태",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MAP",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "행동 가이드",
                    fontSize = 20.sp
                )
            }
        }

        if (showDialog) {

            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },

                title = {
                    Text("행동 가이드")
                },

                text = {
                    Text(
                        "현재 환경 상태는 안전합니다.\n\nFresh Zone 추천:\n중앙도서관 휴게실"
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("확인")
                    }
                }
            )
        }
    }
}