package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.travelhub.features.profile.ProfileViewModel // Import du ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel() // Ajout du ViewModel de profil
) {
    val posts by viewModel.posts.collectAsState()

    // On observe le profil utilisateur pour connaître ses favoris
    val userProfile = profileViewModel.userProfile

    // États pour contrôler l'affichage des fenêtres surgissantes
    var showLikersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    // On récupère le post sélectionné pour l'afficher dans la Sheet de commentaires
    val selectedPost = posts.find { it.id == selectedPostId }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                Text(
                    "TravelShare",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(posts) { post ->
                        // VERIFICATION : Le post actuel est-il dans la liste des favoris ?
                        val isFavorite = userProfile.favorites.contains(post.id)

                        PostItem(
                            post = post,
                            isFavorite = isFavorite, // On passe l'état au composant
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