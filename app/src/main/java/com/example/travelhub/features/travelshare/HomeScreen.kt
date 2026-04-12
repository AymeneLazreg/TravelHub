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
import com.example.travelhub.features.travelshare.components.PostItem
import com.example.travelhub.features.travelshare.components.Comments
import com.example.travelhub.features.travelshare.components.Likes
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    notificationViewModel: NotificationViewModel,
    onNotificationsClick: () -> Unit,
    onUserClick: (String) -> Unit // Ton paramètre est bien là
) {
    val posts by viewModel.posts.collectAsState()
    val userProfile = profileViewModel.userProfile
    val hasUnread = notificationViewModel.hasUnread

    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    val selectedPost = posts.find { it.id == selectedPostId }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

                // --- HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TravelShare", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if (hasUnread) {
                                    Badge(
                                        containerColor = Color.Red,
                                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.Black)
                        }
                    }
                }

                // --- LISTE DES POSTS ---
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
                            // SI TU VEUX CLIQUER SUR L'AUTEUR DU POST :
                            onUserClick = onUserClick,
                            onShowLikers = {
                                viewModel.fetchLikersDetails(post.likedBy)
                                showLikersSheet = true
                            },
                            onDeleteClick = {
                                viewModel.deletePost(post)
                                profileViewModel.loadUserPosts()
                            },
                            onReportClick = { viewModel.reportPost(post) },
                            onFavoriteClick = { viewModel.toggleFavorite(post.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- BOTTOM SHEETS ---
    if (showCommentsSheet && selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            containerColor = Color.White
        ) {
            // C'EST ICI QU'IL MANQUAIT LE PARAMÈTRE (Ligne 115)
            Comments(
                post = selectedPost,
                viewModel = viewModel,
                onUserClick = onUserClick // On transmet la callback ici !
            )
        }
    }

    if (showLikersSheet) {
        val likers by viewModel.likersDetails.collectAsState()
        ModalBottomSheet(
            onDismissRequest = { showLikersSheet = false },
            containerColor = Color.White
        ) {
            // OPTIMISATION : On permet aussi de cliquer sur les profils dans la liste des Likes
            Likes(
                likers = likers,
                onUserClick = { userId ->
                    showLikersSheet = false // On ferme la sheet avant de naviguer
                    onUserClick(userId)
                }
            )
        }
    }

}