package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.travelshare.components.PostItem
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupName: String,
    viewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    // Filtrer les posts pour ce groupe dès l'ouverture
    LaunchedEffect(groupId) {
        viewModel.filterByGroup(groupId)
    }

    val posts by viewModel.filteredPosts.collectAsState()
    val userProfile = profileViewModel.userProfile

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(groupName.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.filterByGroup(null) // Reset pour le fil public
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune publication dans ce groupe", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            isFavorite = userProfile.favorites.contains(post.id),
                            onLikeClick = { viewModel.toggleLike(post) },
                            onCommentClick = { /* Optionnel : ouvrir les commentaires */ },
                            onUserClick = onUserClick,
                            onGroupClick = { _, _ -> /* Déjà dans le groupe, rien à faire */ },
                            onShowLikers = { viewModel.fetchLikersDetails(post.likedBy) },
                            onDeleteClick = { viewModel.deletePost(post) },
                            onReportClick = { viewModel.reportPost(post) },
                            onFavoriteClick = { viewModel.toggleFavorite(post.id) }
                        )
                    }
                }
            }
        }
    }
}