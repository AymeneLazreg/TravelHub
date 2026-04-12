package com.example.travelhub.features.travelshare.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.travelhub.features.travelshare.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailDialog(
    post: Post,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: (String) -> Unit, // <--- AJOUTÉ : Pour la navigation vers le profil
    onShowLikers: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Publication") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PostItem(
                    post = post,
                    isFavorite = isFavorite,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick,
                    onUserClick = onUserClick, // <--- TRANSMIS ICI : Règle l'erreur de compilation
                    onShowLikers = onShowLikers,
                    onDeleteClick = onDeleteClick,
                    onReportClick = onReportClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}