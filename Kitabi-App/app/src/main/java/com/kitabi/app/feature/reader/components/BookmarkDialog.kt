package com.kitabi.app.feature.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * حوار إضافة إشارة مرجعية
 * يسمح للمستخدم بإضافة ملاحظة للإشارة المرجعية
 */
@Composable
fun BookmarkDialog(
    page: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = KitabiTheme.colors.surface,
        title = {
            Text(
                text = "إضافة إشارة مرجعية",
                style = MaterialTheme.typography.titleLarge,
                color = KitabiTheme.colors.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "صفحة $page",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            text = "أضف ملاحظة...",
                            color = KitabiTheme.colors.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KitabiTheme.colors.primary,
                        unfocusedBorderColor = KitabiTheme.colors.outlineVariant
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KitabiTheme.colors.primary,
                    contentColor = KitabiTheme.colors.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "حفظ")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KitabiTheme.colors.surfaceVariant,
                    contentColor = KitabiTheme.colors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "إلغاء")
            }
        }
    )
}
