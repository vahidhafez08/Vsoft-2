package com.vsoft.app.ui.screens.documents

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.DocumentEntity
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditSheet(
    initial: DocumentEntity?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, category: Int, note: String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var category by rememberSaveable(initial?.id) { mutableIntStateOf(initial?.category ?: 4) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        shape = RoundedCornerShape(Dimens.cornerSheet),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .padding(horizontal = Dimens.xl).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initial == null) "افزودن سند" else "ویرایش سند",
                color = Vsoft.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(120) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                label = { Text("عنوان سند") },
                placeholder = { Text("عنوان سند") }
            )
            Text("دسته‌بندی", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentCategories.all.forEachIndexed { index, item ->
                    FilterChip(
                        selected = category == index,
                        onClick = { category = index },
                        label = { Text(item.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Vsoft.colors.surfaceHigh,
                            labelColor = Vsoft.colors.textPrimary,
                            selectedContainerColor = BrandBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(500) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                label = { Text("یادداشت (اختیاری)") }
            )
            Button(
                onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSave(title.trim(), category, note.trim()) },
                enabled = !busy && (initial == null || title.isNotBlank()),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = Color.White, strokeWidth = 2.dp)
                }
                Text("ذخیره")
            }
        }
    }
}
