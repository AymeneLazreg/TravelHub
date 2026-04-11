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
    onDismiss: () -> Unit,
    // On repasse les mêmes actions que pour le PostItem
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShowLikers: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Plein écran
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Publication", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                // On réutilise ton PostItem tel quel !
                PostItem(
                    post = post,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick,
                    onShowLikers = onShowLikers,
                    onDeleteClick = onDeleteClick,
                    onReportClick = onReportClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}