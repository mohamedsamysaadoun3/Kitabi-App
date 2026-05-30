package com.kitabi.app.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.Review
import com.kitabi.app.feature.review.components.RatingDistribution
import com.kitabi.app.feature.review.components.ReviewCard

/**
 * شاشة قائمة مراجعات الكتاب
 * تعرض المراجعات مع توزيع التقييمات وإضافة مراجعة جديدة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    bookId: String,
    onWriteReview: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val reviews by viewModel.getReviews(bookId).collectAsStateWithLifecycle(initialValue = emptyList())
    val averageRating by viewModel.averageRating.collectAsStateWithLifecycle()
    val ratingsCount by viewModel.ratingsCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المراجعات",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onWriteReview,
                containerColor = KitabiTheme.colors.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "كتابة مراجعة",
                    tint = KitabiTheme.colors.onPrimary
                )
            }
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ملخص التقييمات
            item {
                RatingSummaryCard(
                    averageRating = averageRating,
                    ratingsCount = ratingsCount,
                    distribution = viewModel.getRatingDistribution(reviews)
                )
            }

            // قائمة المراجعات
            if (reviews.isEmpty()) {
                item {
                    EmptyReviewsState()
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    ReviewCard(
                        review = review,
                        onLikeClick = { viewModel.likeReview(review.id) },
                        onUnlikeClick = { viewModel.unlikeReview(review.id) }
                    )
                }
            }
        }
    }
}

/**
 * بطاقة ملخص التقييمات
 */
@Composable
private fun RatingSummaryCard(
    averageRating: Float,
    ratingsCount: Int,
    distribution: Map<Int, Int>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // متوسط التقييم
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onSurface
                )
                Row {
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (i < averageRating.toInt()) KitabiTheme.colors.ratingStar else KitabiTheme.colors.outlineVariant
                        )
                    }
                }
                Text(
                    text = "$ratingsCount تقييم",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }

            // توزيع التقييمات
            RatingDistribution(distribution = distribution, totalRatings = ratingsCount)
        }
    }
}

/**
 * حالة عدم وجود مراجعات
 */
@Composable
private fun EmptyReviewsState() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "لا توجد مراجعات بعد",
                style = MaterialTheme.typography.titleMedium,
                color = KitabiTheme.colors.onSurfaceVariant
            )
            Text(
                text = "كن أول من يكتب مراجعة لهذا الكتاب",
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
