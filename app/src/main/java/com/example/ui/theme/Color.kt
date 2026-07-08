package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------------
// PROFESSIONAL POLISH DESIGN THEME CONSTANTS
// -----------------------------------------------------------------------------

// Core Light Palette Theme Colors
val PolishPrimary = Color(0xFF006A62)         // Primary Brand Color: Deep Teal
val PolishOnPrimary = Color(0xFFFFFFFF)       // White text on primary
val PolishPrimaryContainer = Color(0xFFCCE8E5) // Light Mint Teal for User speech bubbles & selections
val PolishOnPrimaryContainer = Color(0xFF05201E) // Dark Teal text

val PolishBackground = Color(0xFFF7F9F8)       // Soft, light professional background canvas
val PolishOnBackground = Color(0xFF191C1B)     // Dark Charcoal text

val PolishSurface = Color(0xFFFFFFFF)          // Pure White for Cards/Sheets/Inputs
val PolishOnSurface = Color(0xFF191C1B)

val PolishSurfaceVariant = Color(0xFFE0E3E1)   // Soft slate grey for sub-panels and chips
val PolishOnSurfaceVariant = Color(0xFF3F4947) // Teal-Grey for secondary labels

val PolishBorder = Color(0xFFDEE3E1)           // Subtle border color

// -----------------------------------------------------------------------------
// COMPATIBILITY ALIAS MAPPINGS FOR ORIGINAL BASECODE
// -----------------------------------------------------------------------------
val NeonCyan = PolishPrimary
val TerminalGreen = Color(0xFF2E7D32)    // Rich professional success green
val ElectricAmber = Color(0xFFC43E00)    // Warning terracotta crimson

val CarbonDark = PolishBackground
val SlateSurface = PolishSurface
val CharcoalVariant = PolishSurfaceVariant

val TextPrimary = PolishOnBackground
val TextSecondary = PolishOnSurfaceVariant
val CodeBorder = PolishBorder

// User/Assistant chat bubble specific color definitions
val UserBubbleBg = PolishPrimaryContainer
val UserBubbleText = PolishOnPrimaryContainer

// Workspace Code Editor Theme Constants (keeps dark high-contrast styling inside message panels)
val PolishCodeBackground = Color(0xFF1E1E1E)  // Code panel editor background
val PolishCodeText = Color(0xFFD4D4D4)        // Code panel text
val PolishCodeGutter = Color(0xFF2D2D30)      // Code panel header/gutter

// Dark Mode Professional Palette (Complementary)
val PolishPrimaryDark = Color(0xFF80D3C9)
val PolishOnPrimaryDark = Color(0xFF003732)
val PolishPrimaryContainerDark = Color(0xFF005049)
val PolishOnPrimaryContainerDark = Color(0xFF9CF0E5)

val PolishBackgroundDark = Color(0xFF191C1B)
val PolishOnBackgroundDark = Color(0xFFE0E3E1)

val PolishSurfaceDark = Color(0xFF202322)
val PolishOnSurfaceDark = Color(0xFFE0E3E1)

val PolishSurfaceVariantDark = Color(0xFF3F4947)
val PolishOnSurfaceVariantDark = Color(0xFFBEC9C6)

val PolishBorderDark = Color(0xFF4A5250)
