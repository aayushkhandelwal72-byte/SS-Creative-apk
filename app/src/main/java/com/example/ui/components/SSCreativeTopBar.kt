package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ColumnTextLightBlue
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.PeacockTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSCreativeTopBar(
    unreadCount: Int,
    isAdminMode: Boolean,
    onToggleAdminMode: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        color = CrispWhite,
        shadowElevation = 2.dp
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ss_creative_logo),
                        contentDescription = "SS Creative Logo",
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("app_logo")
                    )
                }
            },
            actions = {
                // Admin Toggle Mode Chip
                FilterChip(
                    selected = isAdminMode,
                    onClick = onToggleAdminMode,
                    label = {
                        Text(
                            text = if (isAdminMode) "Admin" else "Client",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = ColumnTextLightBlue,
                        selectedLeadingIconColor = PeacockGold
                    ),
                    modifier = Modifier.testTag("admin_mode_chip")
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Notifications with live badge
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("notifications_icon_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = CrispWhite
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = NavyPrimary
                        )
                    }
                }

                // Sign out action
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CrispWhite
            )
        )
    }
}
