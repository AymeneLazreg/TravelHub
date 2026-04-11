package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
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
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Barre de recherche ---
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search photos, places, travelers...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        // --- Filtres de catégories ---
        val filters = listOf("All", "Nature", "City", "Beach", "Museum")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = filter == "All",
                    onClick = { /* Logique de filtrage */ },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Fil d'actualité dynamique ---
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(posts) { post ->
                    // PASSAGE DU VIEWMODEL ICI
                    PostItem(post = post, viewModel = viewModel)
                    HorizontalDivider(
                        color = Color(0xFFEEEEEE),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, viewModel: PostViewModel) {
    // Récupération de l'utilisateur actuel pour vérifier si le post est liké
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = post.likedBy.contains(currentUser?.uid)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // En-tête : Infos utilisateur et lieu
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.userProfileUrl.ifEmpty { "https://ui-avatars.com/api/?name=${post.username}" },
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (post.locationName.isNotEmpty()) {
                        Text(text = " - ${post.locationName}", fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
                Text(text = "Recently", fontSize = 12.sp, color = Color.Gray)
            }

            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image principale
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Travel Photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        if (post.description.isNotEmpty()) {
            Text(text = post.description, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barre d'interactions : Like, Commentaires et Localisation
        Row(verticalAlignment = Alignment.CenterVertically) {

            // BOUTON LIKE INTERACTIF
            IconButton(onClick = { viewModel.toggleLike(post) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(text = "${post.likesCount} likes", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = "Comments",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "0 comments", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Map",
                tint = Color.DarkGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "View Map", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}