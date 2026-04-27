package com.example.travelhub.features.travelshare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(viewModel: GroupViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("") }
    var searchGroupName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        viewModel.fetchUserGroups()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Groupes", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showJoinDialog = true },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Rejoindre")
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Créer")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (viewModel.userGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Vous n'êtes membre d'aucun groupe", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.userGroups) { group ->
                        GroupItem(
                            name = group.name,
                            memberCount = group.members.size,
                            isAdmin = group.adminId == currentUserId,
                            onLeave = {
                                viewModel.leaveGroup(group.id) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = {
                                viewModel.deleteGroup(group.id) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- DIALOG POUR CRÉER ---
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nouveau Groupe") },
                text = {
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("Nom du groupe (Unique)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (groupNameInput.isNotBlank()) {
                            viewModel.createGroup(groupNameInput) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    showCreateDialog = false
                                    groupNameInput = ""
                                }
                            }
                        }
                    }) { Text("Créer", color = Color.Black) }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Annuler") }
                }
            )
        }

        // --- DIALOG POUR REJOINDRE ---
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Rejoindre un groupe") },
                text = {
                    OutlinedTextField(
                        value = searchGroupName,
                        onValueChange = { searchGroupName = it },
                        label = { Text("Entrez le nom exact du groupe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (searchGroupName.isNotBlank()) {
                            viewModel.joinGroupByName(searchGroupName) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    showJoinDialog = false
                                    searchGroupName = ""
                                }
                            }
                        }
                    }) { Text("Rejoindre", color = Color.Black) }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) { Text("Annuler") }
                }
            )
        }
    }
}

@Composable
fun GroupItem(
    name: String,
    memberCount: Int,
    isAdmin: Boolean,
    onLeave: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Group, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF1976D2))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = if (memberCount > 1) "$memberCount membres" else "$memberCount membre",
                    color = Color(0xFF1976D2), fontSize = 12.sp
                )
                if (isAdmin) {
                    Text("Vous êtes l'admin", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Light)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Supprimer le groupe", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Quitter le groupe") },
                            onClick = {
                                showMenu = false
                                onLeave()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                        )
                    }
                }
            }
        }
    }
}