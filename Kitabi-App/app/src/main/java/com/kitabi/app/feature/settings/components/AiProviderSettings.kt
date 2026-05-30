package com.kitabi.app.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * إعدادات مزود الذكاء الاصطناعي
 * يتيح للمستخدم اختيار مزود الذكاء الاصطناعي المفضل
 */
@Composable
fun AiProviderSettings(
    currentProvider: String,
    onProviderChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            // خيار Mistral
            AiProviderOption(
                name = "Mistral Large",
                description = "مجاني وغير محدود • يدعم العربية • محادثة وتلخيص",
                isSelected = currentProvider == "mistral",
                onSelect = { onProviderChange("mistral") }
            )

            // خيار HuggingFace
            AiProviderOption(
                name = "HuggingFace",
                description = "مجاني • ترجمة وتحليل مشاعر • نماذج متخصصة",
                isSelected = currentProvider == "huggingface",
                onSelect = { onProviderChange("huggingface") }
            )

            // خيار تلقائي
            AiProviderOption(
                name = "تلقائي (موصى به)",
                description = "يختار أفضل مزود لكل مهمة تلقائياً",
                isSelected = currentProvider == "auto",
                onSelect = { onProviderChange("auto") }
            )
        }
    }
}

/**
 * خيار مزود ذكاء اصطناعي
 */
@Composable
private fun AiProviderOption(
    name: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = KitabiTheme.colors.primary,
                unselectedColor = KitabiTheme.colors.onSurfaceVariant
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KitabiTheme.colors.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}
