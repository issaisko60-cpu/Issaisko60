package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DaySalesPoint
import com.example.ui.MainViewModel
import com.example.util.AppStrings
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val topCustomers by viewModel.topCustomers.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()

    var selectedSection by remember { mutableStateOf(0) }
    val sectionTitles = listOf(
        "📊 ${AppStrings.get("sales_7_days", lang)}",
        "🏆 ${AppStrings.get("top_products", lang)}",
        "👑 ${AppStrings.get("top_customers", lang)}",
        "🤖 ${AppStrings.get("ai_insights", lang)}"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Level Period Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = AppStrings.get("profit_loss_trend", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportStatItem(
                            label = AppStrings.get("today", lang),
                            revenue = summary.todayRevenue,
                            profit = summary.todayProfit,
                            currency = currency
                        )
                        ReportStatItem(
                            label = AppStrings.get("this_month", lang),
                            revenue = summary.monthRevenue,
                            profit = summary.monthProfit,
                            currency = currency
                        )
                    }
                }
            }
        }

        // Section Selector Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                sectionTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        // Content according to selected tab
        when (selectedSection) {
            0 -> {
                // 7-Day Performance Canvas Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = AppStrings.get("sales_7_days", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            SalesBarChart(
                                data = weeklySales,
                                currency = currency,
                                primaryColor = MaterialTheme.colorScheme.primary,
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }

            1 -> {
                // Top 10 Selling Products
                if (topProducts.isEmpty()) {
                    item {
                        Text(
                            text = if (lang == "ha") "Babu ciniki tukuna" else "No sales data yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(topProducts) { prod ->
                        val index = topProducts.indexOf(prod) + 1
                        val medal = when (index) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$index"
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = medal, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                                    Column {
                                        Text(
                                            text = prod.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${prod.totalQty} da aka sayar",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currency${String.format(Locale.US, "%,.0f", prod.totalSales)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "+$currency${String.format(Locale.US, "%,.0f", prod.totalProfit)} ${AppStrings.get("profit", lang)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Top 10 Customers
                if (topCustomers.isEmpty()) {
                    item {
                        Text(
                            text = if (lang == "ha") "Babu ciniki tukuna" else "No customer sales yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(topCustomers) { cust ->
                        val index = topCustomers.indexOf(cust) + 1
                        val medal = when (index) {
                            1 -> "👑"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$index"
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = medal, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                                    Column {
                                        Text(
                                            text = cust.customerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${cust.salesCount} ${AppStrings.get("sales_count", lang).lowercase()}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "$currency${String.format(Locale.US, "%,.0f", cust.totalSpent)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // AI Smart Recommendations
                item {
                    val bestProd = topProducts.firstOrNull()?.productName
                    val lowItems = lowStock.map { it.name }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InsightCard(
                            icon = Icons.Default.TrendingUp,
                            title = if (lang == "ha") "Kaya Mafi Neman Kasuwa" else "Best-Selling Product",
                            desc = if (bestProd != null) {
                                if (lang == "ha") "🏆 $bestProd ita ce kaya mafi sayarwa a shagonka. Ka tabbatar ajiya ba ta kare ba!"
                                else "🏆 $bestProd is your highest selling product. Ensure continuous supply!"
                            } else {
                                if (lang == "ha") "Yi ciniki don AI ta ba ka shawarar kaya mafi sayarwa."
                                else "Complete sales to get best seller insights."
                            },
                            accentColor = MaterialTheme.colorScheme.primary
                        )

                        if (lowItems.isNotEmpty()) {
                            InsightCard(
                                icon = Icons.Default.WarningAmber,
                                title = if (lang == "ha") "Gargadin Ƙarancin Ajiya" else "Restock Urgency",
                                desc = if (lang == "ha") {
                                    "⚠️ Wadannan kaya suna gab da karewa: ${lowItems.take(3).joinToString(", ")}. Ka yi odar sababbi a yau!"
                                } else {
                                    "⚠️ Items running critically low: ${lowItems.take(3).joinToString(", ")}. Restock soon!"
                                },
                                accentColor = MaterialTheme.colorScheme.error
                            )
                        }

                        InsightCard(
                            icon = Icons.Default.EmojiEvents,
                            title = if (lang == "ha") "Dabarar Bunkasa Ciniki" else "Revenue Boost Tip",
                            desc = if (lang == "ha") {
                                "💡 Bada rangwame ko kyauta ga abokan cinikinka na farko (VIP) don su ci gaba da saye a kai a kai!"
                            } else {
                                "💡 Offer loyalty incentives to your top 10 customers to boost monthly recurring orders!"
                            },
                            accentColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatItem(
    label: String,
    revenue: Double,
    profit: Double,
    currency: String
) {
    Column {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "$currency${String.format(Locale.US, "%,.0f", revenue)}",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Riba: +$currency${String.format(Locale.US, "%,.0f", profit)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun SalesBarChart(
    data: List<DaySalesPoint>,
    currency: String,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(100.0)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (data.isEmpty()) return@Canvas
            val barSpacing = size.width / data.size
            val barWidth = barSpacing * 0.55f

            data.forEachIndexed { i, point ->
                val barHeight = ((point.amount / maxVal).toFloat() * (size.height - 20.dp.toPx())).coerceAtLeast(6f)
                val x = i * barSpacing + (barSpacing - barWidth) / 2
                val y = size.height - barHeight

                // Draw Bar
                drawRoundRect(
                    color = if (i == data.lastIndex) accentColor else primaryColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { point ->
                Text(
                    text = point.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
