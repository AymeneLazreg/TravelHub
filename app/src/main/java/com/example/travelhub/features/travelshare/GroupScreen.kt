package com.example.travelhub.features.travelshare

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.model.Group
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    viewModel: GroupViewModel,
    navController: NavController
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var groupNameInput by remember { mutableStateOf("") }
    var joinGroupNameInput by remember { mutableStateOf("") }

    var selectedGroupImageUri by remember { mutableStateOf<Uri?>(null) }

    var groupToEdit by remember { mutableStateOf<Group?>(null) }
    var editGroupNameInput by remember { mutableStateOf("") }
    var editGroupImageUri by remember { mutableStateOf<Uri?>(null) }

    val userGroups = viewModel.userGroups
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current

    val groupPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedGroupImageUri = uri }
    )

    val editGroupPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> editGroupImageUri = uri }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchUserGroups()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Mes Groupes",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showJoinDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1976D2),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher un groupe"
                    )
                }

                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = { Text("Créer un groupe") }
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else if (userGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.LightGray
                )

                Text(
                    text = "Aucun groupe pour le moment",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(userGroups) { group ->
                    ModernGroupItem(
                        group = group,
                        isAdmin = group.adminId == currentUserId,
                        onClick = {
                            navController.navigate("group_detail/${group.id}/${group.name}")
                        },
                        onEdit = {
                            groupToEdit = group
                            editGroupNameInput = group.name
                            editGroupImageUri = null
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteGroup(group.id) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLeave = {
                            viewModel.leaveGroup(group.id) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nouveau Groupe") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable {
                                    groupPhotoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedGroupImageUri == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )

                                    Text(
                                        text = "Photo",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = selectedGroupImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = groupNameInput,
                            onValueChange = { groupNameInput = it },
                            label = { Text("Nom du groupe") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (groupNameInput.isNotBlank()) {
                                viewModel.createGroup(
                                    name = groupNameInput,
                                    imageUri = selectedGroupImageUri
                                ) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                    if (success) {
                                        showCreateDialog = false
                                        groupNameInput = ""
                                        selectedGroupImageUri = null
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Créer",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCreateDialog = false
                            groupNameInput = ""
                            selectedGroupImageUri = null
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }

        if (showEditDialog && groupToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    groupToEdit = null
                    editGroupNameInput = ""
                    editGroupImageUri = null
                },
                title = { Text("Modifier le groupe") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable {
                                    editGroupPhotoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                editGroupImageUri != null -> {
                                    AsyncImage(
                                        model = editGroupImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                groupToEdit!!.imageUrl.isNotEmpty() -> {
                                    AsyncImage(
                                        model = groupToEdit!!.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                else -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )

                                        Text(
                                            text = "Changer photo",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = editGroupNameInput,
                            onValueChange = { editGroupNameInput = it },
                            label = { Text("Nouveau nom du groupe") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedGroup = groupToEdit

                            if (selectedGroup != null && editGroupNameInput.isNotBlank()) {
                                viewModel.updateGroup(
                                    group = selectedGroup,
                                    newName = editGroupNameInput,
                                    newImageUri = editGroupImageUri
                                ) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                    if (success) {
                                        showEditDialog = false
                                        groupToEdit = null
                                        editGroupNameInput = ""
                                        editGroupImageUri = null
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Enregistrer",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            groupToEdit = null
                            editGroupNameInput = ""
                            editGroupImageUri = null
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }

        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Rejoindre un groupe") },
                text = {
                    OutlinedTextField(
                        value = joinGroupNameInput,
                        onValueChange = { joinGroupNameInput = it },
                        label = { Text("Nom exact du groupe") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (joinGroupNameInput.isNotBlank()) {
                                viewModel.joinGroupByName(joinGroupNameInput) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                    if (success) {
                                        showJoinDialog = false
                                        joinGroupNameInput = ""
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Rejoindre",
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showJoinDialog = false
                            joinGroupNameInput = ""
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun ModernGroupItem(
    group: Group,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLeave: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (group.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = group.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF64B5F6),
                                        Color(0xFF1976D2)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${group.members.size} membres",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                ),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Modifier") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Supprimer",
                                    color = Color.Red
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Quitter") },
                            onClick = {
                                showMenu = false
                                onLeave()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}