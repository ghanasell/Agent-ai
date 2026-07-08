package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(viewModel: AssistantViewModel) {
    val forexAssets by viewModel.forexAssets.collectAsStateWithLifecycle()
    val stockAssets by viewModel.stockAssets.collectAsStateWithLifecycle()
    val memeCoins by viewModel.memeCoins.collectAsStateWithLifecycle()
    val notifications by viewModel.marketNotifications.collectAsStateWithLifecycle()
    val alerts by viewModel.marketAlerts.collectAsStateWithLifecycle()
    
    val selectedAsset by viewModel.selectedMarketAsset.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingMarket.collectAsStateWithLifecycle()
    val analysisResult by viewModel.marketAnalysisResult.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("forex") } // "forex", "stocks", "meme_coins", "alerts"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Text(
            text = "MARKET PULSE & COIN RADAR",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Real-Time Quant Agent & Defi Scanner",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- SUB TABS ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubTabChip(
                text = "Forex Pairs",
                icon = Icons.Default.TrendingUp,
                selected = activeSubTab == "forex",
                onClick = { activeSubTab = "forex" }
            )
            SubTabChip(
                text = "Stock Markets",
                icon = Icons.Default.ShowChart,
                selected = activeSubTab == "stocks",
                onClick = { activeSubTab = "stocks" }
            )
            SubTabChip(
                text = "Meme Radar",
                icon = Icons.Default.Radar,
                selected = activeSubTab == "meme_coins",
                onClick = { activeSubTab = "meme_coins" }
            )
            SubTabChip(
                text = "Alert Hub",
                icon = Icons.Default.NotificationsActive,
                selected = activeSubTab == "alerts",
                onClick = { activeSubTab = "alerts" }
            )
        }

        // --- AI QUANT ANALYSIS DRAWER/CARD PANEL ---
        AnimatedVisibility(
            visible = isAnalyzing || analysisResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = SlateSurface,
                border = BorderStroke(1.dp, CodeBorder),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Audit",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AI QUANT RESEARCH: ${selectedAsset ?: ""}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearMarketAnalysis() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Agent scanning liquidity pools, contract bytecode & macro bias...",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (analysisResult != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF2D2D30), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = analysisResult ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFFD4D4D4),
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SUBTAB CONTENT ---
        when (activeSubTab) {
            "forex" -> ForexTabContent(forexAssets, onAnalyze = { symbol ->
                viewModel.triggerMarketResearch("FOREX", symbol)
            })
            "stocks" -> StocksTabContent(stockAssets, onAnalyze = { symbol, name ->
                viewModel.triggerMarketResearch("STOCK", symbol, name)
            })
            "meme_coins" -> MemeRadarTabContent(memeCoins, onAnalyze = { symbol, name ->
                viewModel.triggerMarketResearch("MEME", symbol, name)
            })
            "alerts" -> AlertHubContent(
                notifications = notifications,
                alerts = alerts,
                onCreateAlert = { symbol, type, condition, value ->
                    viewModel.addAlertSubscription(symbol, type, condition, value)
                },
                onDeleteAlert = { id ->
                    viewModel.deleteAlertSubscription(id)
                }
            )
        }
    }
}

// --- SUB TAB CHIP ---
@Composable
fun SubTabChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) NeonCyan else SlateSurface,
        border = BorderStroke(1.dp, if (selected) NeonCyan else CodeBorder),
        modifier = Modifier.height(42.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (selected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else TextPrimary
                )
            )
        }
    }
}

// --- MINI GRAPH SPARKLINE DRAWING ---
@Composable
fun Sparkline(history: List<Double>, modifier: Modifier = Modifier) {
    if (history.size < 2) return
    Canvas(modifier = modifier) {
        val minVal = history.minOrNull() ?: 0.0
        val maxVal = history.maxOrNull() ?: 1.0
        val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal
        
        val width = size.width
        val height = size.height
        val stepX = width / (history.size - 1)
        
        val points = history.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range * height).toFloat()
            Offset(x, y)
        }
        
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        
        drawPath(
            path = path,
            color = if (history.last() >= history.first()) Color(0xFF4CAF50) else Color(0xFFFF5252),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// --- FOREX TAB ---
@Composable
fun ForexTabContent(
    assets: List<ForexAsset>,
    onAnalyze: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        assets.forEach { asset ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SlateSurface,
                border = BorderStroke(1.dp, CodeBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (asset.symbol.startsWith("EUR")) "🇪🇺/🇺🇸" else if (asset.symbol.startsWith("GBP")) "🇬🇧/🇺🇸" else if (asset.symbol.startsWith("USD/JPY")) "🇺🇸/🇯🇵" else "🇺🇸/🇨🇦",
                                fontSize = 22.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = asset.symbol,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.4f", asset.price),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        )
                    }

                    // Sparkline mini graph
                    Sparkline(
                        history = asset.history,
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1.1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        val isPositive = asset.changePercent >= 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPositive) Color(0x204CAF50) else Color(0x20FF5252),
                            border = BorderStroke(1.dp, if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        ) {
                            Text(
                                text = String.format("%+.2f%%", asset.changePercent),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Button(
                            onClick = { onAnalyze(asset.symbol) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI ANALYZE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- STOCKS TAB ---
@Composable
fun StocksTabContent(
    assets: List<StockAsset>,
    onAnalyze: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        assets.forEach { asset ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SlateSurface,
                border = BorderStroke(1.dp, CodeBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = asset.symbol,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("$%.2f", asset.price),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        )
                    }

                    // Sparkline mini graph
                    Sparkline(
                        history = asset.history,
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1.1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        val isPositive = asset.changePercent >= 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPositive) Color(0x204CAF50) else Color(0x20FF5252),
                            border = BorderStroke(1.dp, if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        ) {
                            Text(
                                text = String.format("%+.2f%%", asset.changePercent),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Button(
                            onClick = { onAnalyze(asset.symbol, asset.name) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI ANALYZE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- MEME COINS TAB ---
@Composable
fun MemeRadarTabContent(
    coins: List<MemeCoinAsset>,
    onAnalyze: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // RADAR ONLINE SCANNER TOP HEADER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1A3038),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MEME SCANNER RADAR ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "Scanning newly injected liquidity pools on decentralized exchanges...",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBEC9C6))
                    )
                }
            }
        }

        coins.forEach { coin ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SlateSurface,
                border = BorderStroke(1.dp, if (coin.isLaunched) CodeBorder else NeonCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(coin.logoColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = coin.symbol.take(2),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = coin.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "DEX: ${coin.symbol}/WETH",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        // Launch Countdown Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (coin.isLaunched) Color(0x2000E676) else Color(0x20FF9100),
                            border = BorderStroke(1.dp, if (coin.isLaunched) Color(0xFF00E676) else Color(0xFFFF9100))
                        ) {
                            Text(
                                text = coin.launchTime.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (coin.isLaunched) Color(0xFF00E676) else Color(0xFFFF9100)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = CodeBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Current Price", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                text = if (coin.isLaunched) String.format("$%.8f", coin.currentPrice) else "LAUNCHING SOON",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (coin.isLaunched) TextPrimary else TextSecondary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Initial Liquidity", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                text = String.format("$%,.0f", coin.liquidity),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contract Safety Parameters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonDark, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Honeypot: " + (if (coin.honeypot) "🔴 YES" else "🟢 NO"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tax: ${coin.buyTax}% / ${coin.sellTax}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Risk: ${coin.riskScore}/100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (coin.riskScore > 60) Color(0xFFFF5252) else if (coin.riskScore > 30) Color(0xFFFFC107) else Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onAnalyze(coin.symbol, coin.name) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI DEFI DEEP AUDIT & FORENSICS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}

// --- ALERTS HUB CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertHubContent(
    notifications: List<MarketNotification>,
    alerts: List<MarketAlertSubscription>,
    onCreateAlert: (String, String, String, Double) -> Unit,
    onDeleteAlert: (String) -> Unit
) {
    var symbolInput by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf("STOCK") } // "FOREX", "STOCK", "MEME"
    var condition by remember { mutableStateOf("ABOVE") } // "ABOVE", "BELOW", "LAUNCH"
    var targetValueInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // --- ADD ALERT PANEL ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CREATE CUSTOM MARKET SUBSCRIPTION ALERT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = symbolInput,
                    onValueChange = { symbolInput = it.uppercase() },
                    label = { Text("Asset Ticker/Symbol (e.g. TSLA, EUR/USD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Asset Type Selector Chips
                Text("Asset Category:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("FOREX", "STOCK", "MEME").forEach { type ->
                        FilterChip(
                            selected = alertType == type,
                            onClick = { alertType = type },
                            label = { Text(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Condition Selector
                Text("Condition:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ABOVE", "BELOW", "LAUNCH").forEach { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (condition != "LAUNCH") {
                    OutlinedTextField(
                        value = targetValueInput,
                        onValueChange = { targetValueInput = it },
                        label = { Text("Target Threshold Value ($ or price)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (symbolInput.isNotEmpty()) {
                            val targetVal = targetValueInput.toDoubleOrNull() ?: 0.0
                            onCreateAlert(symbolInput, alertType, condition, targetVal)
                            symbolInput = ""
                            targetValueInput = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CREATE ACTIVE RADAR SUBSCRIPTION", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        // --- ACTIVE ALERT SUBSCRIPTIONS LIST ---
        if (alerts.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SlateSurface,
                border = BorderStroke(1.dp, CodeBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ACTIVE RADAR SUBSCRIPTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    alerts.forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (alert.isActive) Color(0x2000E676) else Color(0x20757575)
                                    ) {
                                        Text(
                                            text = if (alert.isActive) "ACTIVE" else "TRIGGERED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (alert.isActive) Color(0xFF00E676) else Color(0xFF757575),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = alert.assetSymbol,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )
                                }
                                Text(
                                    text = "${alert.alertType} • Notify when ${alert.condition.lowercase()}" + (if (alert.condition != "LAUNCH") " \$${alert.targetValue}" else ""),
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteAlert(alert.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Alert", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            }
                        }
                        Divider(color = CodeBorder.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // --- NOTIFICATION HISTORICAL LOGS ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ALERTS LOG & NOTIFICATION TIMELINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No alert notifications logged yet.\nScanner is actively parsing markets...",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    notifications.forEach { note ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (note.type == "MEME") Icons.Default.Radar else if (note.type == "STOCK") Icons.Default.ShowChart else Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (note.type == "MEME") Color(0xFFFF9100) else NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )
                                }
                                Text(
                                    text = note.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                            )
                        }
                        Divider(color = CodeBorder.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
