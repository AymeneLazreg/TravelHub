package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun Comments(
    post: Post,
    viewModel: PostViewModel,
    onUserClick: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    // Vérification de l'état de connexion (null = anonyme)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isAnonymous = currentUser == null
    val currentUserId = currentUser?.uid ?: ""

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedComment by remember { mutableStateOf<Comment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding()
    ) {
        Text("Commentaires", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Liste des commentaires (Visible par tout le monde)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            if (post.comments.isEmpty()) {
                item {
                    Text("Aucun commentaire.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                items(post.comments) { comment ->
                    CommentRow(
                        comment = comment,
                        isMyComment = comment.userId == currentUserId,
                        onUserClick = onUserClick,
                        onLongPress = {
                            if (!isAnonymous) { // Seul un utilisateur connecté peut interagir au appui long
                                selectedComment = comment
                                if (comment.userId == currentUserId) showDeleteDialog = true
                                else showReportDialog = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ZONE DE SAISIE : Affichée uniquement si l'utilisateur n'est pas anonyme
        if (!isAnonymous) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer",
                        tint = Color(0xFF007AFF)
                    )
                }
            }
        } else {
            // Message optionnel pour les anonymes
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Connectez-vous pour ajouter un commentaire",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // Dialogue de suppression
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

    // Note: showReportDialog peut être implémenté ici de la même manière si nécessaire
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentRow(
    comment: Comment,
    isMyComment: Boolean,
    onUserClick: (String) -> Unit,
    onLongPress: () -> Unit
) {
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
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { onUserClick(comment.userId) },
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formatTimestamp(comment.timestamp.seconds * 1000),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    if (seconds < 60) return "à l'instant"
    if (minutes < 60) return "${minutes}min"
    if (hours < 24) return "${hours}h"
    return "${days}j"
}