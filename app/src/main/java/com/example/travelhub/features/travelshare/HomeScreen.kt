package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelhub.features.travelshare.components.PostItem
import com.example.travelhub.features.travelshare.components.Comments
import com.example.travelhub.features.travelshare.components.Likes
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.example.travelhub.features.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    onNotificationsClick: () -> Unit // Ajout de la navigation vers les notifications
) {
    val posts by viewModel.posts.collectAsState()
    val userProfile = profileViewModel.userProfile

    // États pour contrôler l'affichage des fenêtres surgissantes
    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    val selectedPost = posts.find { it.id == selectedPostId }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

                // --- BARRE SUPÉRIEURE (HEADER) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TravelShare",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ICÔNE NOTIFICATIONS
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Black
                        )
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(posts) { post ->
                        val isFavorite = userProfile.favorites.contains(post.id)

                        PostItem(
                            post = post,
                            isFavorite = isFavorite,
                            onLikeClick = { viewModel.toggleLike(post) },
                            onCommentClick = {
                                selectedPostId = post.id
                                showCommentsSheet = true
                            },
                            onShowLikers = {
                                viewModel.fetchLikersDetails(post.likedBy)
                                showLikersSheet = true
                            },
                            onDeleteClick = { viewModel.deletePost(post) },
                            onReportClick = { viewModel.reportPost(post) },
                            onFavoriteClick = { viewModel.toggleFavorite(post.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- FENÊTRE DES COMMENTAIRES ---
    if (showCommentsSheet && selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            containerColor = Color.White
        ) {
            Comments(post = selectedPost, viewModel = viewModel)
        }
    }

    // --- FENÊTRE DES LIKES ---
    if (showLikersSheet) {
        val likers by viewModel.likersDetails.collectAsState()
        ModalBottomSheet(
            onDismissRequest = { showLikersSheet = false },
            containerColor = Color.White
        ) {
            Likes(likers = likers)
        }
    }
}