package com.example.travelhub.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.components.PostDetailDialog
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    profileViewModel: ProfileViewModel,
    postViewModel: PostViewModel,
    onUserClick: (String) -> Unit
) {
    val userProfile = profileViewModel.userProfile
    val isLoading = profileViewModel.isLoading

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPostForDetail by remember { mutableStateOf<Post?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            // --- HEADER DU PROFIL ---
            Column(modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "@${userProfile.username}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogoutClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Déconnexion",
                            tint = Color.Black
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.photoUrl,
                            contentDescription = "Photo",
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color.Gray)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        ProfileStat(count = profileViewModel.userPosts.size.toString(), label = "Posts")
                        ProfileStat(count = userProfile.favorites.size.toString(), label = "Favoris")
                    }
                }

                Text(text = "${userProfile.prenom} ${userProfile.nom}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = userProfile.bio, fontSize = 14.sp)

                // LA LIGNE PARASITE A ÉTÉ SUPPRIMÉE ICI

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.LocationOn, modifier = Modifier.size(16.dp), contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = userProfile.location, color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
                ) {
                    Text("Modifier le profil", fontWeight = FontWeight.SemiBold)
                }
            }

            // --- ONGLETS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color.Black
                    )
                }
            ) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Mes Posts") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Favoris") })
            }

            val currentGridPosts = if (selectedTabIndex == 0) profileViewModel.userPosts else profileViewModel.favoritePosts

            if (currentGridPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                    Text("Aucun post pour le moment", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentGridPosts, key = { it.id }) { post ->
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .clickable { selectedPostForDetail = post },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGUE DE DÉTAIL ---
    selectedPostForDetail?.let { post ->
        val isFavorite = userProfile.favorites.contains(post.id)

        PostDetailDialog(
            post = post,
            isFavorite = isFavorite,
            viewModel = postViewModel,
            onDismiss = { selectedPostForDetail = null },
            onLikeClick = { postViewModel.toggleLike(post) },
            onCommentClick = { /* Action gérée dans le Dialog */ },
            onUserClick = onUserClick, // Cette ligne-là est la bonne !
            onShowLikers = { postViewModel.fetchLikersDetails(post.likedBy) },
            onDeleteClick = {
                selectedPostForDetail = null
                postViewModel.deletePost(post)
                profileViewModel.removePostLocally(post.id)
            },
            onReportClick = { postViewModel.reportPost(post) },
            onFavoriteClick = { postViewModel.toggleFavorite(post.id) }
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}