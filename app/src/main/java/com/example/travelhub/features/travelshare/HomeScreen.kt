package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PostViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val likers by viewModel.likersDetails.collectAsState()

    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            // Barre de recherche (simplifiée pour l'exemple)
            Text("TravelShare", modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        viewModel = viewModel,
                        onLikesClick = { selectedPost = post; viewModel.fetchLikersDetails(post.likedBy); showLikersSheet = true },
                        onCommentsClick = { selectedPost = post; showCommentsSheet = true }
                    )
                }
            }
        }

        // --- BOTTOM SHEET LIKERS ---
        if (showLikersSheet) {
            ModalBottomSheet(onDismissRequest = { showLikersSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.5f)) {
                    Text("Aimé par", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (selectedPost?.likedBy?.isEmpty() == true) {
                        Text("Personne n'a encore aimé ce post", modifier = Modifier.padding(top = 20.dp))
                    } else {
                        LazyColumn { items(likers) { user -> LikerRow(user) } }
                    }
                }
            }
        }

        // --- BOTTOM SHEET COMMENTAIRES ---
        if (showCommentsSheet && selectedPost != null) {
            ModalBottomSheet(onDismissRequest = { showCommentsSheet = false }) {
                CommentSheetContent(post = selectedPost!!, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CommentSheetContent(post: Post, viewModel: PostViewModel) {
    var commentText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Commentaires", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            if (post.comments.isEmpty()) {
                item { Text("Aucun commentaire. Soyez le premier !", color = Color.Gray) }
            } else {
                items(post.comments) { comment ->
                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                        AsyncImage(
                            model = comment.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${comment.username}" },
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(comment.text, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.imePadding()) {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Ajouter un commentaire...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
            )
            IconButton(onClick = {
                if (commentText.isNotBlank()) {
                    viewModel.addComment(post.id, commentText)
                    commentText = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF007AFF))
            }
        }
    }
}

@Composable
fun PostItem(post: Post, viewModel: PostViewModel, onLikesClick: () -> Unit, onCommentsClick: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = post.likedBy.contains(currentUser?.uid)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = post.userProfileUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
            Text(post.username, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
        }

        // Image
        AsyncImage(
            model = post.imageUrl, contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp).padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.toggleLike(post) }) {
                Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (isLiked) Color.Red else Color.Black)
            }
            Text("${post.likesCount} likes", modifier = Modifier.clickable { onLikesClick() }, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = onCommentsClick) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null)
            }
            Text("${post.comments.size}", modifier = Modifier.clickable { onCommentsClick() }, fontWeight = FontWeight.Bold)
        }

        Text(post.description, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun LikerRow(user: UserProfile) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.Gray))
        Spacer(modifier = Modifier.width(12.dp))
        Text(user.username, fontWeight = FontWeight.Bold)
    }
}