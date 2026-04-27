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
import com.example.travelhub.features.travelshare.components.PostDetailDialog
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    notificationViewModel: NotificationViewModel,
    groupViewModel: GroupViewModel = viewModel(),
    onNotificationsClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onGroupClick: (String, String) -> Unit // AJOUTÉ POUR LA NAVIGATION
) {
    val posts by viewModel.filteredPosts.collectAsState()
    val userProfile = profileViewModel.userProfile
    val hasUnread by remember { derivedStateOf { notificationViewModel.hasUnread } }

    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    val selectedPost = posts.find { it.id == selectedPostId }

    LaunchedEffect(Unit) {
        viewModel.filterByGroup(null)
        groupViewModel.fetchUserGroups()
    }

    LaunchedEffect(viewModel.selectedPostIdFromNotif) {
        val postIdFromNotif = viewModel.selectedPostIdFromNotif
        if (postIdFromNotif != null) {
            selectedPostId = postIdFromNotif
            if (viewModel.shouldOpenCommentsFromNotif) {
                delay(600)
            }
            viewModel.clearNavigationRequest()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TravelShare", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)

                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (hasUnread) {
                                Badge(containerColor = Color.Red)
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.Black)
                    }
                }
            }

            // --- LISTE DES POSTS MIXTES ---
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune publication pour le moment", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(posts, key = { it.id }) { post ->
                        val isFavorite = userProfile.favorites.contains(post.id)
                        PostItem(
                            post = post,
                            isFavorite = isFavorite,
                            onLikeClick = { viewModel.toggleLike(post) },
                            onCommentClick = {
                                selectedPostId = post.id
                                showCommentsSheet = true
                            },
                            onUserClick = onUserClick,
                            onGroupClick = onGroupClick, // NAVIGATION VERS LE GROUPE
                            onShowLikers = {
                                viewModel.fetchLikersDetails(post.likedBy)
                                showLikersSheet = true
                                selectedPostId = post.id
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

    // --- BOTTOM SHEETS & DIALOGS ---
    if (showCommentsSheet && selectedPost != null) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            containerColor = Color.White
        ) {
            Comments(post = selectedPost, viewModel = viewModel, onUserClick = onUserClick)
        }
    }

    if (showLikersSheet) {
        val likers by viewModel.likersDetails.collectAsState()
        ModalBottomSheet(
            onDismissRequest = { showLikersSheet = false },
            containerColor = Color.White
        ) {
            Likes(likers = likers, onUserClick = { userId ->
                showLikersSheet = false
                onUserClick(userId)
            })
        }
    }

    if (selectedPost != null && !showCommentsSheet && !showLikersSheet) {
        PostDetailDialog(
            post = selectedPost,
            isFavorite = userProfile.favorites.contains(selectedPost.id),
            viewModel = viewModel,
            onDismiss = { selectedPostId = null },
            onLikeClick = { viewModel.toggleLike(selectedPost) },
            onCommentClick = { showCommentsSheet = true },
            onUserClick = onUserClick,
            onShowLikers = {
                viewModel.fetchLikersDetails(selectedPost.likedBy)
                showLikersSheet = true
            },
            onDeleteClick = {
                viewModel.deletePost(selectedPost)
                profileViewModel.loadUserPosts()
                selectedPostId = null
            },
            onReportClick = { viewModel.reportPost(selectedPost) },
            onFavoriteClick = { viewModel.toggleFavorite(selectedPost.id) }
        )
    }
}