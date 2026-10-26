package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.repository.SSCreativeRepository
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.components.SSCreativeTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.GstBillingScreen
import com.example.ui.screens.PaintStudioScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.appSignOut
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.SSCreativeTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.GstBillingViewModel
import com.example.viewmodel.NotificationsViewModel
import com.example.viewmodel.PaintStudioViewModel
import com.example.viewmodel.PortfolioViewModel
import com.example.viewmodel.ProjectsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            SSCreativeTheme {
                AppRootContent()
            }
        }
    }
}

@Composable
fun AppRootContent() {
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        MainPortalContent(user = user)
    }
}

@Composable
fun MainPortalContent(user: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val repository = remember(user.uid) { SSCreativeRepository(context) }

    // Seed default showcase items if database is clean
    LaunchedEffect(user.uid) {
        repository.seedInitialDataIfEmpty(user.uid, user.displayName ?: "Valued Client")
    }

    // ViewModels scoped to authenticated session
    val portfolioViewModel = remember(user.uid) { PortfolioViewModel(repository) }
    val projectsViewModel = remember(user.uid) { ProjectsViewModel(repository, user.uid) }
    val chatViewModel = remember(user.uid) { ChatViewModel(repository, user.uid, user.displayName ?: "Client") }
    val notificationsViewModel = remember(user.uid) { NotificationsViewModel(repository, user.uid) }
    val adminViewModel = remember { AdminViewModel(repository) }
    var isAdminMode by remember { mutableStateOf(false) }
    val gstBillingViewModel = remember(user.uid, isAdminMode) {
        GstBillingViewModel(repository, user.uid, isAdmin = isAdminMode)
    }
    val paintStudioViewModel = remember { PaintStudioViewModel() }

    val notifications by notificationsViewModel.notifications.collectAsState()
    val unreadNotifsCount = notifications.count { !it.read }

    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var activeChatQuery by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = isAdminMode || selectedNavIndex != 0) {
        if (isAdminMode) {
            isAdminMode = false
        } else {
            selectedNavIndex = 0
        }
    }

    Scaffold(
        topBar = {
            SSCreativeTopBar(
                unreadCount = unreadNotifsCount,
                isAdminMode = isAdminMode,
                onToggleAdminMode = {
                    isAdminMode = !isAdminMode
                },
                onOpenNotifications = {
                    showNotificationsSheet = true
                },
                onSignOut = {
                    appSignOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = { /* Listener handles UI reset */ },
                        scope = scope
                    )
                }
            )
        },
        bottomBar = {
            if (!isAdminMode) {
                NavigationBar(
                    containerColor = CrispWhite,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("client_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = selectedNavIndex == 0,
                        onClick = { selectedNavIndex = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DesignServices,
                                contentDescription = "Portfolio"
                            )
                        },
                        label = {
                            Text(
                                "Portfolio",
                                fontWeight = if (selectedNavIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyContainer,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_portfolio")
                    )

                    NavigationBarItem(
                        selected = selectedNavIndex == 1,
                        onClick = { selectedNavIndex = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Projects"
                            )
                        },
                        label = {
                            Text(
                                "Projects",
                                fontWeight = if (selectedNavIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyContainer,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_projects")
                    )

                    NavigationBarItem(
                        selected = selectedNavIndex == 2,
                        onClick = { selectedNavIndex = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "GST Bills"
                            )
                        },
                        label = {
                            Text(
                                "GST Bills",
                                fontWeight = if (selectedNavIndex == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyContainer,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_gst_bills")
                    )

                    NavigationBarItem(
                        selected = selectedNavIndex == 3,
                        onClick = { selectedNavIndex = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = "Paint Studio"
                            )
                        },
                        label = {
                            Text(
                                "Paint Studio",
                                fontWeight = if (selectedNavIndex == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyContainer,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_paint_studio")
                    )

                    NavigationBarItem(
                        selected = selectedNavIndex == 4,
                        onClick = { selectedNavIndex = 4 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Nidhi AI Agent"
                            )
                        },
                        label = {
                            Text(
                                "Nidhi AI",
                                fontWeight = if (selectedNavIndex == 4) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyContainer,
                            unselectedIconColor = PeacockGold,
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_chat")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAdminMode) {
                AdminScreen(
                    viewModel = adminViewModel,
                    gstBillingViewModel = gstBillingViewModel,
                    currentUserId = user.uid
                )
            } else {
                when (selectedNavIndex) {
                    0 -> {
                        PortfolioScreen(
                            viewModel = portfolioViewModel,
                            onConsultWithNidhi = { query ->
                                activeChatQuery = query
                                selectedNavIndex = 4
                            }
                        )
                    }
                    1 -> {
                        ProjectsScreen(
                            viewModel = projectsViewModel,
                            onConsultWithNidhi = { query ->
                                activeChatQuery = query
                                selectedNavIndex = 4
                            }
                        )
                    }
                    2 -> {
                        GstBillingScreen(
                            viewModel = gstBillingViewModel,
                            onConsultWithNidhi = { query ->
                                activeChatQuery = query
                                selectedNavIndex = 4
                            }
                        )
                    }
                    3 -> {
                        PaintStudioScreen(
                            viewModel = paintStudioViewModel,
                            onConsultWithNidhi = { query ->
                                activeChatQuery = query
                                selectedNavIndex = 4
                            }
                        )
                    }
                    4 -> {
                        ChatScreen(
                            viewModel = chatViewModel,
                            initialQuery = activeChatQuery,
                            onQueryHandled = { activeChatQuery = null }
                        )
                    }
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onMarkAsRead = { notifId ->
                notificationsViewModel.markAsRead(notifId)
            }
        )
    }
}
