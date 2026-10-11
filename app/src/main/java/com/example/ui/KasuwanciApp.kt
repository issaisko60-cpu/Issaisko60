package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.screens.*
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasuwanciApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val lang by viewModel.language.collectAsState()
    val celebrationNotice by viewModel.celebrationNotice.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsScreen by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_kasuwanci_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.get("app_title", lang),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (showSettingsScreen) AppStrings.get("settings", lang)
                                    else when (currentTab) {
                                        AppTab.DASHBOARD -> AppStrings.get("dashboard", lang)
                                        AppTab.SALES -> AppStrings.get("pos_sale", lang)
                                        AppTab.PRODUCTS -> AppStrings.get("products", lang)
                                        AppTab.CUSTOMERS -> AppStrings.get("customers", lang)
                                        AppTab.REPORTS -> AppStrings.get("reports", lang)
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Language Switch Chip
                        TextButton(
                            onClick = {
                                viewModel.setLanguage(if (lang == "ha") "en" else "ha")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (lang == "ha") "🇳🇬 HA" else "🇬🇧 EN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Settings Icon Button
                        IconButton(
                            onClick = { showSettingsScreen = !showSettingsScreen }
                        ) {
                            Icon(
                                if (showSettingsScreen) Icons.Default.Close else Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (showSettingsScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!showSettingsScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.DASHBOARD,
                            onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = AppStrings.get("dashboard", lang)
                                )
                            },
                            label = { Text(AppStrings.get("dashboard", lang), fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_dashboard")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.SALES,
                            onClick = { viewModel.setTab(AppTab.SALES) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.SALES) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                    contentDescription = AppStrings.get("pos_sale", lang)
                                )
                            },
                            label = { Text(AppStrings.get("pos_sale", lang), fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_sales")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.PRODUCTS,
                            onClick = { viewModel.setTab(AppTab.PRODUCTS) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.PRODUCTS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                    contentDescription = AppStrings.get("products", lang)
                                )
                            },
                            label = { Text(AppStrings.get("products", lang), fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_products")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.CUSTOMERS,
                            onClick = { viewModel.setTab(AppTab.CUSTOMERS) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.CUSTOMERS) Icons.Filled.People else Icons.Outlined.People,
                                    contentDescription = AppStrings.get("customers", lang)
                                )
                            },
                            label = { Text(AppStrings.get("customers", lang), fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_customers")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.REPORTS,
                            onClick = { viewModel.setTab(AppTab.REPORTS) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.REPORTS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                    contentDescription = AppStrings.get("reports", lang)
                                )
                            },
                            label = { Text(AppStrings.get("reports", lang), fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.testTag("nav_reports")
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
                if (showSettingsScreen) {
                    SettingsScreen(viewModel = viewModel)
                } else {
                    when (currentTab) {
                        AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        AppTab.SALES -> SalesScreen(viewModel = viewModel)
                        AppTab.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                        AppTab.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                        AppTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    }
                }

                // Celebratory Modal Popup
                celebrationNotice?.let { notice ->
                    CelebrationBanner(
                        message = notice,
                        onDismiss = { viewModel.dismissCelebration() }
                    )
                }
            }
        }
    }
}

@Composable
fun CelebrationBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 🌟 🏆 💫 🎯",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "An sami nasara a cikin tsarin kasuwancinku!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ci Gaba / Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
