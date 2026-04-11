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
import com.example.travelhub.features.travelshare.components.Comments
import com.example.travelhub.features.travelshare.components.Likes
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PostViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val likers by viewModel.likersDetails.collectAsState()

    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Text("TravelShare", modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        viewModel = viewModel,
                        onLikesClick = {
                            selectedPostId = post.id
                            viewModel.fetchLikersDetails(post.likedBy)
                            showLikersSheet = true
                        },
                        onCommentsClick = {
                            selectedPostId = post.id
                            showCommentsSheet = true
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }

        if (showLikersSheet) {
            ModalBottomSheet(onDismissRequest = { showLikersSheet = false }) {
                Likes(likers = likers)
            }
        }

        if (showCommentsSheet && selectedPostId != null) {
            ModalBottomSheet(onDismissRequest = { showCommentsSheet = false }) {
                // SOLUTION : On cherche le post en direct dans la liste du ViewModel
                val livePost = posts.find { it.id == selectedPostId }
                livePost?.let {
                    Comments(post = it, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, viewModel: PostViewModel, onLikesClick: () -> Unit, onCommentsClick: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = post.likedBy.contains(currentUser?.uid)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${post.username}" },
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Text(post.username, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
        }

        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
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