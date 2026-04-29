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
import com.example.travelhub.features.travelshare.components.PostDetailDialog
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
    // Filtrer les posts pour ce groupe dès l'ouverture [cite: 18, 42]
    LaunchedEffect(groupId) {
        viewModel.filterByGroup(groupId)
    }

    // On récupère les posts filtrés et le profil utilisateur pour les favoris
    val posts by viewModel.filteredPosts.collectAsState()
    val userProfile = profileViewModel.userProfile

    // État pour gérer l'affichage de la fiche détaillée
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    val selectedPost = posts.find { it.id == selectedPostId }

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
                            onCommentClick = {
                                selectedPostId = post.id // Ouvre le détail pour voir les commentaires [cite: 37]
                            },
                            onUserClick = onUserClick,
                            onGroupClick = { _, _ -> /* Déjà dans le groupe */ },
                            onImageClick = { selectedPostId = post.id }, // Déclenche l'ouverture du dialogue
                            onShowLikers = { viewModel.fetchLikersDetails(post.likedBy) },
                            onDeleteClick = { viewModel.deletePost(post) },
                            onReportClick = { viewModel.reportPost(post) }, // Option de signalement [cite: 15, 36]
                            onFavoriteClick = { viewModel.toggleFavorite(post.id) }
                        )
                    }
                }
            }
        }
    }

    // --- AFFICHAGE DE LA FICHE DÉTAILLÉE ---
    // Cette partie permet d'afficher les infos du lieu, la date et le bouton itinéraire [cite: 14, 20, 44]
    if (selectedPost != null) {
        PostDetailDialog(
            post = selectedPost,
            isFavorite = userProfile.favorites.contains(selectedPost.id),
            viewModel = viewModel,
            onDismiss = { selectedPostId = null },
            onLikeClick = { viewModel.toggleLike(selectedPost) },
            onCommentClick = { /* Déjà géré dans le dialogue */ },
            onUserClick = onUserClick,
            onShowLikers = { viewModel.fetchLikersDetails(selectedPost.likedBy) },
            onDeleteClick = {
                viewModel.deletePost(selectedPost)
                selectedPostId = null
            },
            onReportClick = { viewModel.reportPost(selectedPost) },
            onFavoriteClick = { viewModel.toggleFavorite(selectedPost.id) }
        )
    }
}