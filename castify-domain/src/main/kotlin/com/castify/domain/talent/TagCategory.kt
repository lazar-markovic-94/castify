package com.castify.domain.talent

enum class TagCategory {
    PRIMARY,
    SECONDARY
}

data class CategorizedTag(
    val name: String,
    val category: TagCategory
)

object PredefinedTags {
    val PRIMARY = setOf(
        "acting",
        "voiceover"
    )

    val SECONDARY = setOf(
        "photoshooting",
        "videoshooting",
        "fullvideoproduction"
    )

    val ALL_TAGS = mapOf(
        TagCategory.PRIMARY to PRIMARY,
        TagCategory.SECONDARY to SECONDARY
    )

    fun isValidTag(tagName: String): Boolean {
        return PRIMARY.contains(tagName) || SECONDARY.contains(tagName)
    }

    fun getCategoryForTag(tagName: String): TagCategory? {
        return when {
            PRIMARY.contains(tagName) -> TagCategory.PRIMARY
            SECONDARY.contains(tagName) -> TagCategory.SECONDARY
            else -> null
        }
    }

    fun getAllValidTags(): Set<String> {
        return PRIMARY + SECONDARY
    }
}
