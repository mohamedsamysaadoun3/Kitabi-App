package com.kitabi.app.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.feature.review.components.RatingBar

/**
 * شاشة كتابة مراجعة
 * تتيح للمستخدم كتابة مراجعة وتقييم الكتاب
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    bookId: String,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    var rating by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "كتابة مراجعة",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = KitabiTheme.colors.onSurface
                        )
                    }
                },
                actions = {
                    Text(
                        text = "نشر",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rating > 0 && content.isNotBlank()) {
                            KitabiTheme.colors.primary
                        } else {
                            KitabiTheme.colors.onSurfaceVariant
                        },
                        modifier = Modifier
                            .clickable(enabled = rating > 0 && content.isNotBlank() && !isSubmitting) {
                                isSubmitting = true
                                viewModel.submitReview(bookId, rating, title, content) { success ->
                                    isSubmitting = false
                                    if (success) onSubmitSuccess()
                                }
                            }
                            .padding(horizontal = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface
                )
            )
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // التقييم
            Text(
                text = "تقييمك",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = KitabiTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            RatingBar(
                rating = rating,
                onRatingChange = { rating = it },
                starSize = 40.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // عنوان المراجعة
            Text(
                text = "عنوان المراجعة",
                style = MaterialTheme.typography.titleSmall,
                color = KitabiTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "لخص رأيك في جملة واحدة",
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KitabiTheme.colors.primary,
                    unfocusedBorderColor = KitabiTheme.colors.outlineVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // محتوى المراجعة
            Text(
                text = "المراجعة",
                style = MaterialTheme.typography.titleSmall,
                color = KitabiTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        text = "شارك رأيك المفصل عن الكتاب...",
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KitabiTheme.colors.primary,
                    unfocusedBorderColor = KitabiTheme.colors.outlineVariant
                ),
                maxLines = 10
            )
        }
    }
}
