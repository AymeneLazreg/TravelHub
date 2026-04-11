package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: PostViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val likers by viewModel.likersDetails.collectAsState()

    var showLikersSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            // --- Barre de recherche ---
            OutlinedTextField(
                value = "", onValueChange = {},
                placeholder = { Text("Search photos, places...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // --- Fil d'actualité ---
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            viewModel = viewModel,
                            onLikesCountClick = {
                                viewModel.fetchLikersDetails(post.likedBy)
                                showLikersSheet = true
                            }
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        // --- MODAL LISTE DES LIKERS ---
        if (showLikersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLikersSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.6f)) {
                    Text("Aimé par", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (likers.isEmpty()) {
                        Text("Chargement...", color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(likers) { user ->
                                LikerRow(user)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun LikerRow(user: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.photoUrl.ifEmpty { "https://ui-avatars.com/api/?name=${user.username}" },
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = user.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "${user.prenom} ${user.nom}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun PostItem(post: Post, viewModel: PostViewModel, onLikesCountClick: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = post.likedBy.contains(currentUser?.uid)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // En-tête utilisateur
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${post.username}" },
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (post.locationName.isNotEmpty()) {
                    Text(text = post.locationName, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image du Post
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.toggleLike(post) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color.Red else Color.Black
                )
            }
            // TEXTE CLIQUABLE POUR VOIR LA LISTE
            Text(
                text = "${post.likesCount} likes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLikesCountClick() }
            )

            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(20.dp))
        }

        if (post.description.isNotEmpty()) {
            Text(
                text = post.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}