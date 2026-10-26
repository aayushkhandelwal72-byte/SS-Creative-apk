package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import com.example.ui.theme.navyTextFieldColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientProject
import com.example.data.model.PortfolioItem
import com.example.ui.theme.ColumnTextLightBlue
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.LightBlue
import com.example.ui.theme.LightBlueSoft
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.PeacockTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    gstBillingViewModel: com.example.viewmodel.GstBillingViewModel,
    currentUserId: String
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Projects", "Portfolio", "GST Billing", "Broadcast")
    val allProjects by viewModel.allProjects.collectAsState()
    val allPortfolio by viewModel.allPortfolio.collectAsState()
    val adminMessage by viewModel.adminMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddPortfolioDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ClientProject?>(null) }

    LaunchedEffect(adminMessage) {
        adminMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddProjectDialog = true },
                    containerColor = NavyPrimary,
                    contentColor = CrispWhite,
                    modifier = Modifier.testTag("add_project_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Project")
                }
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddPortfolioDialog = true },
                    containerColor = NavyPrimary,
                    contentColor = CrispWhite,
                    modifier = Modifier.testTag("add_portfolio_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Portfolio")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Admin Banner Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PeacockGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SS CREATIVE ADMIN STUDIO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PeacockGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Content & Project Management Control",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColumnTextLightBlue
                        )
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CrispWhite,
                contentColor = NavyPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NavyPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    AdminProjectsTab(
                        projects = allProjects,
                        onEditProject = { editingProject = it }
                    )
                }
                1 -> {
                    AdminPortfolioTab(
                        portfolioItems = allPortfolio,
                        onDeleteItem = { viewModel.deletePortfolioItem(it) }
                    )
                }
                2 -> {
                    GstBillingScreen(viewModel = gstBillingViewModel)
                }
                3 -> {
                    AdminBroadcastTab(
                        currentUserId = currentUserId,
                        onSendNotification = { targetUser, title, msg ->
                            viewModel.sendBroadcastNotification(targetUser, title, msg)
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddProjectDialog) {
        AddProjectDialog(
            defaultClientId = currentUserId,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { clientId, clientName, title, category, status, progress, start, target, desc ->
                viewModel.createProject(clientId, clientName, title, category, status, progress, start, target, desc)
                showAddProjectDialog = false
            }
        )
    }

    if (showAddPortfolioDialog) {
        AddPortfolioDialog(
            onDismiss = { showAddPortfolioDialog = false },
            onConfirm = { title, category, desc, client, tags, featured, color, imageUrl ->
                viewModel.addPortfolioItem(title, category, desc, client, tags, featured, color, imageUrl)
                showAddPortfolioDialog = false
            }
        )
    }

    editingProject?.let { project ->
        EditProjectProgressDialog(
            project = project,
            onDismiss = { editingProject = null },
            onConfirm = { newStatus, newProgress ->
                viewModel.updateProjectProgress(
                    projectId = project.id,
                    clientId = project.clientId,
                    projectTitle = project.title,
                    newStatus = newStatus,
                    newProgress = newProgress
                )
                editingProject = null
            }
        )
    }
}

@Composable
private fun AdminProjectsTab(
    projects: List<ClientProject>,
    onEditProject: (ClientProject) -> Unit
) {
    if (projects.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No projects found. Tap '+' to create one for a client.", color = TextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(projects) { project ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            IconButton(onClick = { onEditProject(project) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Progress",
                                    tint = PeacockTeal
                                )
                            }
                        }
                        Text(
                            text = "Client: ${project.clientName} (UID: ${project.clientId.take(8)}...)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status: ${project.status}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PeacockTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${project.progressPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPortfolioTab(
    portfolioItems: List<PortfolioItem>,
    onDeleteItem: (String) -> Unit
) {
    if (portfolioItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No portfolio items. Tap '+' to add showcase work.", color = TextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(portfolioItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "${item.category} • Client: ${item.clientName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PeacockTeal
                            )
                        }
                        IconButton(onClick = { onDeleteItem(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBroadcastTab(
    currentUserId: String,
    onSendNotification: (String, String, String) -> Unit
) {
    var targetUser by remember { mutableStateOf(currentUserId) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Broadcast Client Notification",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Text(
            text = "Send milestone updates or studio announcements directly to client notifications.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val tfColors = navyTextFieldColors()

        OutlinedTextField(
            value = targetUser,
            onValueChange = { targetUser = it },
            label = { Text("Client User ID") },
            colors = tfColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title") },
            colors = tfColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message Body") },
            colors = tfColors,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && message.isNotBlank()) {
                    onSendNotification(targetUser, title, message)
                    title = ""
                    message = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = PeacockGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Real-Time Notification")
        }
    }
}

@Composable
private fun EditProjectProgressDialog(
    project: ClientProject,
    onDismiss: () -> Unit,
    onConfirm: (newStatus: String, newProgress: Long) -> Unit
) {
    var status by remember { mutableStateOf(project.status) }
    var progressSlider by remember { mutableFloatStateOf(project.progressPercent.toFloat()) }
    val statuses = listOf("Discovery & Briefing", "Creative Design", "In Progress", "Client Review", "Completed & Delivered")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update: ${project.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Progress: ${progressSlider.toInt()}%", fontWeight = FontWeight.Bold, color = PeacockTeal)
                Slider(
                    value = progressSlider,
                    onValueChange = { progressSlider = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = PeacockTeal)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Stage / Status:", fontWeight = FontWeight.SemiBold)
                statuses.forEach { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Checkbox(checked = status == s, onCheckedChange = { if (it) status = s })
                        Text(s, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(status, progressSlider.toLong()) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save & Notify Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddProjectDialog(
    defaultClientId: String,
    onDismiss: () -> Unit,
    onConfirm: (clientId: String, clientName: String, title: String, category: String, status: String, progress: Long, start: String, target: String, desc: String) -> Unit
) {
    var clientId by remember { mutableStateOf(defaultClientId) }
    var clientName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Branding") }
    var status by remember { mutableStateOf("In Progress") }
    var targetDate by remember { mutableStateOf("15 Nov 2026") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Client Project", fontWeight = FontWeight.Bold) },
        text = {
            val tfColors = navyTextFieldColors()
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client Name / Brand") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Video, Web, Branding)") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = targetDate, onValueChange = { targetDate = it }, label = { Text("Target Delivery Date") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description & Deliverables") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(clientId, clientName.ifBlank { "Client" }, title, category, status, 20L, "25 Oct 2026", targetDate, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Create Project")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddPortfolioDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, desc: String, client: String, tags: List<String>, featured: Boolean, color: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Branding & Identity") }
    var clientName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("Branding, Logo, Visual") }
    var imageUrl by remember { mutableStateOf("") }
    var featured by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Item", fontWeight = FontWeight.Bold) },
        text = {
            val tfColors = navyTextFieldColors()
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client / Brand Name") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Project Image URL (Optional)") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Case Study Description") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = tagsInput, onValueChange = { tagsInput = it }, label = { Text("Tags (comma-separated)") }, colors = tfColors, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = featured, onCheckedChange = { featured = it })
                    Text("Featured on Studio Home")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onConfirm(title, category, description, clientName, tags, featured, "#072048", imageUrl.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Add Showcase")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
