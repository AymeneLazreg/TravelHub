package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import androidx.compose.ui.text.font.FontWeight
// Assure-toi que cet import est présent pour le composant Comments
import com.example.travelhub.features.travelshare.components.Comments

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailDialog(
    post: Post,
    isFavorite: Boolean,
    viewModel: PostViewModel,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onShowLikers: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    // 1. État local pour gérer l'affichage de la sheet des commentaires
    var showInternalComments by remember { mutableStateOf(false) }

    // 2. LOGIQUE D'AUTO-OUVERTURE (Déclenchée à l'ouverture du dialogue)
    LaunchedEffect(key1 = viewModel.shouldOpenCommentsFromNotif) {
        if (viewModel.shouldOpenCommentsFromNotif) {
            // Un léger délai permet au Dialog de se stabiliser avant d'ouvrir la sheet
            kotlinx.coroutines.delay(100)
            showInternalComments = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Plein écran
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Publication", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PostItem(
                    post = post,
                    isFavorite = isFavorite,
                    onLikeClick = onLikeClick,
                    onCommentClick = {
                        // Ouvre la sheet interne quand on clique sur l'icône commentaire
                        showInternalComments = true
                    },
                    onUserClick = onUserClick,
                    onShowLikers = onShowLikers,
                    onDeleteClick = onDeleteClick,
                    onReportClick = onReportClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }

        // 3. LA SHEET DES COMMENTAIRES (S'affiche par-dessus le PostDetailDialog)
        if (showInternalComments) {
            ModalBottomSheet(
                onDismissRequest = { showInternalComments = false },
                containerColor = Color.White
                // On a supprimé la ligne windowInsets qui faisait l'erreur
            ) {
                Comments(
                    post = post,
                    viewModel = viewModel,
                    onUserClick = onUserClick
                )
            }
        }
    }
}