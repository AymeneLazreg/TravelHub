package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.model.Comment
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Comments(post: Post, viewModel: PostViewModel) {
    var commentText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // États pour les actions
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedComment by remember { mutableStateOf<Comment?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp).navigationBarsPadding()) {
        Text("Commentaires", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            if (post.comments.isEmpty()) {
                item { Text("Aucun commentaire.", color = Color.Gray, fontSize = 14.sp) }
            } else {
                items(post.comments) { comment ->
                    CommentRow(
                        comment = comment,
                        isMyComment = comment.userId == currentUserId,
                        onLongPress = {
                            selectedComment = comment
                            if (comment.userId == currentUserId) {
                                showDeleteDialog = true
                            } else {
                                showReportDialog = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().imePadding(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Écrire un commentaire...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            IconButton(onClick = {
                if (commentText.isNotBlank()) {
                    viewModel.addComment(post, commentText)
                    commentText = ""
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF007AFF))
            }
        }
    }

    // --- DIALOGUE DE SUPPRESSION ---
    if (showDeleteDialog && selectedComment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer ?") },
            text = { Text("Voulez-vous supprimer votre commentaire ?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteComment(post.id, selectedComment!!)
                    showDeleteDialog = false
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    // --- DIALOGUE DE SIGNALEMENT ---
    if (showReportDialog && selectedComment != null) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Signaler ce commentaire ?") },
            text = { Text("Ce commentaire va être envoyé à la modération pour examen.") },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                }) { Text("Signaler", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentRow(comment: Comment, isMyComment: Boolean, onLongPress: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { onLongPress() }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${comment.username}" },
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                // On multiplie par 1000 pour transformer les secondes Firebase en millisecondes Kotlin
                Text(formatTimestamp(comment.timestamp.seconds * 1000), fontSize = 12.sp, color = Color.Gray)
            }
            Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

// FONCTION UTILITAIRE POUR LA DATE
fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        seconds < 60 -> "à l'instant"
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}j"
        else -> "${weeks}s"
    }
}