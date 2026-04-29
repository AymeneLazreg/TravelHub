package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PostItem(
    post: Post,
    isFavorite: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onGroupClick: (String, String) -> Unit,
    onImageClick: () -> Unit, // AJOUTÉ : Pour ouvrir le détail
    onShowLikers: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isLiked = post.likedBy.contains(currentUserId)
    val relativeTime = getRelativeTime(post.timestamp.toDate())
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${post.username}" },
                    contentDescription = null,
                    modifier = Modifier.size(42.dp).clip(CircleShape).clickable { onUserClick(post.userId) },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.username,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable { onUserClick(post.userId) }
                        )

                        if (!post.groupId.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp).padding(horizontal = 2.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = (post.groupName ?: "Groupe").replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.clickable {
                                    onGroupClick(post.groupId, post.groupName ?: "Groupe")
                                }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.locationName,
                            fontSize = 12.sp,
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = " • $relativeTime", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (post.userId == currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                onClick = { showMenu = false; onDeleteClick() }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Signaler", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Report, null, tint = Color.Red) },
                                onClick = { showMenu = false; onReportClick() }
                            )
                        }
                    }
                }
            }

            // --- IMAGE CLIQUABLE ---
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onImageClick() }, // ACTION AJOUTÉE ICI
                contentScale = ContentScale.Crop
            )

            // --- ACTIONS ---
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(CircleShape).clickable { onLikeClick() }.padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) Color.Red else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = "${post.likesCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(CircleShape).clickable { onCommentClick() }.padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${post.comments.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color(0xFFFFD700) else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (post.description.isNotBlank()) {
                Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 20.dp)) {
                    Text(text = post.description, fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

fun getRelativeTime(date: java.util.Date): String {
    val now = java.util.Date().time
    val diff = now - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "maintenant"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}j"
        else -> "${days / 7}s"
    }
}