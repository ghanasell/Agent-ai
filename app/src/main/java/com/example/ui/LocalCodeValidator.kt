package com.example.ui

enum class LocalWarningSeverity {
    ERROR, WARNING, INFO
}

data class LocalWarning(
    val message: String,
    val severity: LocalWarningSeverity,
    val line: Int? = null
)

object LocalCodeValidator {
    fun validate(code: String): List<LocalWarning> {
        if (code.trim().isEmpty()) return emptyList()
        val warnings = mutableListOf<LocalWarning>()
        
        // 1. Balanced Brackets & Braces Check
        val braceStack = mutableListOf<Char>()
        val braceLines = mutableListOf<Int>()
        var currentLine = 1
        
        var inSingleLineComment = false
        var inMultiLineComment = false
        var inStringLiteral = false
        var inCharLiteral = false
        var isEscaped = false
        
        var i = 0
        val len = code.length
        while (i < len) {
            val c = code[i]
            
            // Handle newlines
            if (c == '\n') {
                currentLine++
                inSingleLineComment = false
                isEscaped = false
                i++
                continue
            }
            
            // Handle escape characters inside strings
            if (inStringLiteral && c == '\\') {
                isEscaped = !isEscaped
                i++
                continue
            }
            
            // Comment transitions
            if (!inStringLiteral && !inCharLiteral) {
                if (!inMultiLineComment && !inSingleLineComment && c == '/' && i + 1 < len) {
                    if (code[i + 1] == '/') {
                        inSingleLineComment = true
                        i += 2
                        continue
                    } else if (code[i + 1] == '*') {
                        inMultiLineComment = true
                        i += 2
                        continue
                    }
                }
                
                if (inMultiLineComment && c == '*' && i + 1 < len && code[i + 1] == '/') {
                    inMultiLineComment = false
                    i += 2
                    continue
                }
            }
            
            // Skip commented out code
            if (inSingleLineComment || inMultiLineComment) {
                i++
                continue
            }
            
            // String & Char literals transitions
            if (c == '"' && !isEscaped) {
                inStringLiteral = !inStringLiteral
            } else if (c == '\'' && !isEscaped) {
                inCharLiteral = !inCharLiteral
            }
            
            isEscaped = false
            
            // Parse braces if we're not inside a literal or comment
            if (!inStringLiteral && !inCharLiteral) {
                when (c) {
                    '{', '(', '[' -> {
                        braceStack.add(c)
                        braceLines.add(currentLine)
                    }
                    '}' -> {
                        if (braceStack.isNotEmpty() && braceStack.last() == '{') {
                            braceStack.removeAt(braceStack.lastIndex)
                            braceLines.removeAt(braceLines.lastIndex)
                        } else {
                            warnings.add(LocalWarning("Mismatched closing curly brace '}' without opening '{'", LocalWarningSeverity.ERROR, currentLine))
                        }
                    }
                    ')' -> {
                        if (braceStack.isNotEmpty() && braceStack.last() == '(') {
                            braceStack.removeAt(braceStack.lastIndex)
                            braceLines.removeAt(braceLines.lastIndex)
                        } else {
                            warnings.add(LocalWarning("Mismatched closing parenthesis ')' without opening '('", LocalWarningSeverity.ERROR, currentLine))
                        }
                    }
                    ']' -> {
                        if (braceStack.isNotEmpty() && braceStack.last() == '[') {
                            braceStack.removeAt(braceStack.lastIndex)
                            braceLines.removeAt(braceLines.lastIndex)
                        } else {
                            warnings.add(LocalWarning("Mismatched closing bracket ']' without opening '['", LocalWarningSeverity.ERROR, currentLine))
                        }
                    }
                }
            }
            
            i++
        }
        
        // Check for remaining unclosed elements in stack
        while (braceStack.isNotEmpty()) {
            val unclosed = braceStack.removeAt(braceStack.lastIndex)
            val line = braceLines.removeAt(braceLines.lastIndex)
            val name = when (unclosed) {
                '{' -> "curly brace '{'"
                '(' -> "parenthesis '('"
                '[' -> "bracket '['"
                else -> "token '$unclosed'"
            }
            warnings.add(LocalWarning("Unclosed $name starting here", LocalWarningSeverity.ERROR, line))
        }
        
        if (inStringLiteral) {
            warnings.add(LocalWarning("Unclosed double-quotes (\") string literal found", LocalWarningSeverity.ERROR))
        }
        if (inCharLiteral) {
            warnings.add(LocalWarning("Unclosed single-quotes (') character literal found", LocalWarningSeverity.ERROR))
        }
        if (inMultiLineComment) {
            warnings.add(LocalWarning("Unclosed multi-line comment (/*) found", LocalWarningSeverity.WARNING))
        }
        
        // 2. Common logical warnings for code blocks
        if (code.contains("fun ") && !code.contains("{") && !code.contains("abstract ") && !code.contains("interface ")) {
            warnings.add(LocalWarning("Function declaration may be missing a body or implementation block", LocalWarningSeverity.WARNING))
        }
        
        return warnings
    }
}

data class CodeIssue(
    val type: String,        // "Error" | "Warning" | "Suggestion"
    val line: String,        // line number or "Unknown"
    val summary: String,
    val explanation: String,
    val fix: String
)

object CodeIssueParser {
    fun parseCodeIssues(output: String): List<CodeIssue> {
        val issues = mutableListOf<CodeIssue>()
        val regex = "\\[ISSUE\\]([\\s\\S]*?)\\[/ISSUE\\]".toRegex()
        val matches = regex.findAll(output)
        for (match in matches) {
            val block = match.groups[1]?.value ?: continue
            
            var type = "Error"
            var line = "Unknown"
            var summary = ""
            var explanation = ""
            var fix = ""
            
            block.lines().forEach { lineText ->
                val trimmed = lineText.trim()
                when {
                    trimmed.startsWith("Type:") -> type = trimmed.removePrefix("Type:").trim()
                    trimmed.startsWith("Line:") -> line = trimmed.removePrefix("Line:").trim()
                    trimmed.startsWith("Summary:") -> summary = trimmed.removePrefix("Summary:").trim()
                    trimmed.startsWith("Explanation:") -> explanation = trimmed.removePrefix("Explanation:").trim()
                    trimmed.startsWith("Fix:") -> fix = trimmed.removePrefix("Fix:").trim()
                }
            }
            
            if (summary.isNotEmpty()) {
                issues.add(CodeIssue(type, line, summary, explanation, fix))
            }
        }
        return issues
    }

    fun parseFixedCode(output: String): String? {
        val regex = "\\[FIXED_CODE\\]([\\s\\S]*?)\\[/FIXED_CODE\\]".toRegex()
        val match = regex.find(output)
        return match?.groups[1]?.value?.trim()
    }
}

