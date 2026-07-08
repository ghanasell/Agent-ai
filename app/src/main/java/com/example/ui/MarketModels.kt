package com.example.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ForexAsset(
    val symbol: String,
    val price: Double,
    val changePercent: Double,
    val history: List<Double>,
    val high: Double,
    val low: Double
)

data class StockAsset(
    val symbol: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val history: List<Double>,
    val high: Double,
    val low: Double
)

data class MemeCoinAsset(
    val symbol: String,
    val name: String,
    val launchTime: String, // format e.g. "14:20:00"
    val countdownSeconds: Int,
    val launchPrice: Double,
    val currentPrice: Double,
    val marketCap: Double,
    val liquidity: Double,
    val buyTax: Double,
    val sellTax: Double,
    val honeypot: Boolean,
    val isLaunched: Boolean,
    val riskScore: Int, // 0 to 100 (lower is safer)
    val logoColor: Long // for visual presentation
)

data class MarketNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
    val type: String // "FOREX" | "STOCK" | "MEME"
)

data class MarketAlertSubscription(
    val id: String = java.util.UUID.randomUUID().toString(),
    val assetSymbol: String,
    val alertType: String, // "FOREX" | "STOCK" | "MEME"
    val condition: String, // "ABOVE" | "BELOW" | "LAUNCH"
    val targetValue: Double,
    val isActive: Boolean = true
)
