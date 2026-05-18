package com.example.travelhub.features.travelshare.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map // NOUVEL IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel

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
    onFavoriteClick: () -> Unit,
    onCreateItineraryClick: (String) -> Unit // <--- NOUVEAU CALLBACK
) {
    var showInternalComments by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.shouldOpenCommentsFromNotif) {
        if (viewModel.shouldOpenCommentsFromNotif) {
            kotlinx.coroutines.delay(100)
            showInternalComments = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                PostItem(
                    post = post,
                    isFavorite = isFavorite,
                    onLikeClick = onLikeClick,
                    onCommentClick = { showInternalComments = true },
                    onUserClick = onUserClick,
                    onGroupClick = { _, _ -> },
                    onImageClick = { },
                    onShowLikers = onShowLikers,
                    onDeleteClick = onDeleteClick,
                    onReportClick = onReportClick,
                    showFullDescription = true,
                    onFavoriteClick = onFavoriteClick
                )

                if (post.category.isNotBlank() || post.tags.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (post.category.isNotBlank()) {
                            Text(
                                text = "Catégorie",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = post.category,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        if (post.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Tags",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(post.tags) { tag ->
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = "#$tag",
                                                fontSize = 13.sp
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val destination = if (post.fullAddress.isNotBlank()) {
                            Uri.encode(post.fullAddress)
                        } else {
                            "${post.latitude},${post.longitude}"
                        }

                        val gmmIntentUri = Uri.parse("google.navigation:q=$destination")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Comment y aller", fontWeight = FontWeight.Bold)
                }

                // --- NOUVEAU BOUTON : CRÉER UN PARCOURS (Version Ville + Pays uniquement) ---
                Button(
                    onClick = {
                        // 1. On découpe l'adresse complète à chaque virgule
                        val addressParts = post.fullAddress.split(",")

                        // 2. On extrait intelligemment la ville et le pays
                        val cityAndCountry = if (addressParts.size >= 2) {
                            // On prend les deux derniers blocs (souvent " 75000 Paris" et " France")
                            val lastTwo = addressParts.takeLast(2).joinToString(",")
                            // On efface les chiffres (comme le code postal) pour avoir juste "Paris, France"
                            lastTwo.replace(Regex("\\d+"), "").trim()
                        } else {
                            post.fullAddress // Sécurité si l'adresse est très courte
                        }

                        // 3. On envoie ce texte propre à la navigation
                        onCreateItineraryClick(cityAndCountry)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Créer un parcours ici", fontWeight = FontWeight.Bold, color = Color.White)
                }
                // -------------------------------------------------------------------------

                val similarPosts = remember(post) { viewModel.getSimilarPosts(post) }

                if (similarPosts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )

                    Text(
                        text = "Photos similaires",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(similarPosts) { similarPost ->
                            AsyncImage(
                                model = similarPost.imageUrl,
                                contentDescription = "Photo similaire",
                                modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showInternalComments) {
            ModalBottomSheet(
                onDismissRequest = { showInternalComments = false },
                containerColor = Color.White
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