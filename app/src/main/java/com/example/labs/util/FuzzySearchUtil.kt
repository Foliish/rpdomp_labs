package com.example.labs.util

object FuzzySearchUtil {

    /**
     * Calculates the Levenshtein distance between two strings.
     */
    fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1].lowercaseChar() == rhs[i - 1].lowercaseChar()) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = minOf(minOf(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }

    /**
     * Returns true if [query] is a fuzzy match for [text].
     * Tolerance is how many allowed typos (distance).
     */
    fun fuzzyMatch(query: String, text: String, tolerance: Int = 2): Boolean {
        if (query.isBlank()) return true
        if (text.isBlank()) return false
        
        val q = query.lowercase().trim()
        val t = text.lowercase()

        // Exact substring match check first (for fast path)
        if (t.contains(q)) return true

        // If not exact substring, check word by word using Levenshtein distance
        val words = t.split(Regex("\\s+"))
        for (word in words) {
            if (levenshtein(q, word) <= tolerance) {
                return true
            }
        }
        
        // Also check full text just in case
        return levenshtein(q, t) <= tolerance * 2
    }
}
