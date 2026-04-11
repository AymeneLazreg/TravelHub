package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // <-- IMPORTANT
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.model.Notification
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.utils.getRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    // --- DÉCLENCHEUR : MARQUER COMME LU À L'OUVERTURE ---
    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
    }

    val notifications = viewModel.notifications
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Black)
            } else if (notifications.isEmpty()) {
                Text(
                    "Aucune notification pour le moment",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onDelete = { viewModel.deleteNotification(notification.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onDelete: () -> Unit) {
    val message = when (notification.type) {
        "LIKE" -> "a aimé votre publication."
        "COMMENT" -> "a commenté : \"${notification.commentText}\""
        "FAVORITE" -> "a ajouté votre publication à ses favoris."
        else -> "a interagi avec vous."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Optionnel : Fond légèrement bleuté si non lu pour le contraste
            .background(if (notification.read) Color.White else Color(0xFFF7F9FB))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = notification.fromUserProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${notification.fromUsername}" },
            contentDescription = null,
            modifier = Modifier.size(45.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${notification.fromUsername} $message",
                fontSize = 14.sp,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
            )
            Text(
                text = getRelativeTime(notification.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        AsyncImage(
            model = notification.postImageUrl,
            contentDescription = null,
            modifier = Modifier.size(45.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
    }
}