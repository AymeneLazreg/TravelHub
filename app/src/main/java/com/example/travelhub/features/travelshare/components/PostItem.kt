package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShowLikers: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onFavoriteClick: () -> Unit // <-- AJOUTÉ : Action pour les favoris
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isLiked = post.likedBy.contains(currentUserId)
    val relativeTime = getRelativeTime(post.timestamp.toDate())

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // --- HEADER ---
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${post.username}" },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.username, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("• $relativeTime", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(post.locationName, fontSize = 12.sp, color = Color.Gray)
                }

                // --- BOUTON 3 POINTS ---
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (post.userId == currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        } else {
                            // OPTION FAVORIS (Remplace Enregistrer)
                            DropdownMenuItem(
                                text = { Text("Ajouter aux favoris") },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700)) },
                                onClick = {
                                    showMenu = false
                                    onFavoriteClick() // Déclenche l'ajout en base
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Signaler", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }

            // --- IMAGE ---
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )

            // --- ACTIONS (LIKE & COMMENT) ---
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isLiked) Color.Red else Color.Black
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onShowLikers() }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCommentClick) {
                        Icon(
                            imageVector = Icons.Outlined.ModeComment,
                            contentDescription = "Commentaires",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${post.comments.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // --- DESCRIPTION ---
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                Text(text = post.description, fontSize = 14.sp)
                if (post.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.tags.joinToString(" ") { "#$it" },
                        color = Color(0xFF007AFF),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// --- FONCTION UTILITAIRE  ---
fun getRelativeTime(date: Date): String {
    val now = Date().time
    val diff = now - date.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        seconds < 60 -> "maintenant"
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}j"
        else -> "${weeks}s"
    }
}