package com.kitabi.app.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * شاشة التعريف الأولي
 * 3 صفحات جميلة تقدم ميزات التطبيق
 * مع انتقالات سلسة ومؤشر نقاط
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.AutoStories,
            emoji = "📚",
            title = "اكتشف عالم الكتب",
            description = "استكشف آلاف الكتب العربية في مختلف التصنيفات. من الأدب إلى العلوم، من التاريخ إلى الفلسفة.",
            gradientColors = listOf(
                KitabiTheme.colors.primary,
                KitabiTheme.colors.tertiary
            )
        ),
        OnboardingPage(
            icon = Icons.Filled.Psychology,
            emoji = "🤖",
            title = "مساعدك الذكي",
            description = "تلخيص ذكي، شرح مفصل، ترجمة فورية، وأسئلة تفاعلية. مساعدك الذكي يرافقك في رحلة القراءة.",
            gradientColors = listOf(
                KitabiTheme.colors.tertiary,
                KitabiTheme.colors.secondary
            )
        ),
        OnboardingPage(
            icon = Icons.Filled.Chat,
            emoji = "💬",
            title = "شارك واناقش",
            description = "ناقش الكتب مع قراء آخرين، شارك اقتباساتك، واكتشف آراء جديدة في غرف المحادثة.",
            gradientColors = listOf(
                KitabiTheme.colors.secondary,
                KitabiTheme.colors.primary
            )
        )
    )

    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        KitabiTheme.colors.background,
                        page.gradientColors[0].copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // الأيقونة/الإيموجي
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.gradientColors[0].copy(alpha = 0.2f),
                                page.gradientColors[1].copy(alpha = 0.1f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // العنوان
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // الوصف
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = KitabiTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // مؤشر النقاط
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .background(
                                if (index == currentPage) KitabiTheme.colors.primary
                                else KitabiTheme.colors.outlineVariant,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // أزرار التنقل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر التخطي
                if (currentPage < pages.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "تخطي",
                            color = KitabiTheme.colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // زر التالي/ابدأ
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KitabiTheme.colors.primary,
                        contentColor = KitabiTheme.colors.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "التالي" else "ابدأ الآن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * بيانات صفحة التعريف
 */
data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val emoji: String,
    val title: String,
    val description: String,
    val gradientColors: List<androidx.compose.ui.graphics.Color>
)
