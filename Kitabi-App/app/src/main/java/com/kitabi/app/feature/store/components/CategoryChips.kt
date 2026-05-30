package com.kitabi.app.feature.store.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * شرائح التصنيفات الأفقية
 * تعرض تصنيفات الكتب في شريط أفقي قابل للتمرير
 */
@Composable
fun CategoryChips(
    categories: List<CategoryChipItem>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategory
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category.id) },
                shape = RoundedCornerShape(24.dp),
                color = if (isSelected) {
                    KitabiTheme.colors.primary
                } else {
                    KitabiTheme.colors.surfaceVariant
                },
                contentColor = if (isSelected) {
                    KitabiTheme.colors.onPrimary
                } else {
                    KitabiTheme.colors.onSurfaceVariant
                }
            ) {
                Text(
                    text = "${category.icon} ${category.name}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * عنصر شريحة التصنيف
 */
data class CategoryChipItem(
    val id: String,
    val name: String,
    val icon: String
)

/**
 * التصنيفات المتاحة في المتجر
 */
val storeCategories = listOf(
    CategoryChipItem("all", "الكل", "📚"),
    CategoryChipItem("fiction", "روايات وأدب", "📖"),
    CategoryChipItem("science", "علوم", "🔬"),
    CategoryChipItem("technology", "تكنولوجيا", "💻"),
    CategoryChipItem("history", "تاريخ", "🏛️"),
    CategoryChipItem("philosophy", "فلسفة", "🤔"),
    CategoryChipItem("psychology", "علم نفس", "🧠"),
    CategoryChipItem("religion", "دين", "🕌"),
    CategoryChipItem("poetry", "شعر", "✍️"),
    CategoryChipItem("children", "أطفال", "🧒"),
    CategoryChipItem("education", "تعليم", "🎓"),
    CategoryChipItem("business", "أعمال", "💼"),
    CategoryChipItem("self_help", "تطوير ذات", "🌟"),
    CategoryChipItem("translated", "مترجم", "🌐"),
    CategoryChipItem("arabic_original", "عربي أصيل", "🇪🇬"),
    CategoryChipItem("public_domain", "ملكية عامة", "📜"),
    CategoryChipItem("academic", "أكاديمي", "🎓")
)
