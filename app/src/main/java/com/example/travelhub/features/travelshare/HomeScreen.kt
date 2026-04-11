package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.travelhub.features.travelshare.model.Comment
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
            Text("TravelShare", modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        viewModel = viewModel,
                        onLikesClick = {
                            selectedPost = post
                            viewModel.fetchLikersDetails(post.likedBy)
                            showLikersSheet = true
                        },
                        onCommentsClick = {
                            selectedPost = post
                            showCommentsSheet = true
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }

        // --- SHEET LIKERS (TA RÉFÉRENCE PARFAITE) ---
        if (showLikersSheet) {
            ModalBottomSheet(onDismissRequest = { showLikersSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Aimé par", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedPost?.likedBy?.isEmpty() == true) {
                        Text("Personne n'a aimé ce post", color = Color.Gray)
                    } else {
                        LazyColumn { items(likers) { user -> LikerRow(user) } }
                    }
                }
            }
        }

        // --- SHEET COMMENTAIRES (COPIÉE SUR LE MODÈLE DES LIKES) ---
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

    // On utilise exactement la même structure que pour les Likes
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding()
    ) {
        Text("Commentaires", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // On utilise LazyColumn SANS poids (weight) pour qu'elle se comporte comme la liste des Likes
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (post.comments.isEmpty()) {
                item {
                    Text("Aucun commentaire. Soyez le premier !", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                items(post.comments) { comment ->
                    CommentRow(comment)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
        Spacer(modifier = Modifier.height(16.dp))

        // Zone de saisie
        Row(
            modifier = Modifier.fillMaxWidth().imePadding(),
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

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(post.id, commentText)
                        commentText = ""
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (commentText.isNotBlank()) Color(0xFF007AFF) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun CommentRow(comment: Comment) {
    Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = comment.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${comment.username}" },
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun PostItem(post: Post, viewModel: PostViewModel, onLikesClick: () -> Unit, onCommentsClick: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = post.likedBy.contains(currentUser?.uid)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = post.userProfileUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
            Text(post.username, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
        }

        AsyncImage(
            model = post.imageUrl, contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp).padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

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
        if (post.description.isNotBlank()) {
            Text(post.description, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun LikerRow(user: UserProfile) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.Gray), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(12.dp))
        Text(user.username, fontWeight = FontWeight.Bold)
    }
}