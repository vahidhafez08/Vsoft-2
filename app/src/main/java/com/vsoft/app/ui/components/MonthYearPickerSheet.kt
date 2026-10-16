package com.vsoft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.navigationBarsPadding
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.JCal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerSheet(
    initialYear: Int,
    initialMonth: Int,
    onPick: (year: Int, month: Int) -> Unit,
    onToday: () -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableIntStateOf(initialYear.coerceIn(1300, 1500)) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { year = (year - 1).coerceAtLeast(1300) }, enabled = year > 1300) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "سال قبل", tint = Vsoft.colors.textPrimary)
                }
                Text(
                    JCal.fa(year), color = Vsoft.colors.textPrimary, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { year = (year + 1).coerceAtMost(1500) }, enabled = year < 1500) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "سال بعد", tint = Vsoft.colors.textPrimary)
                }
            }
            repeat(4) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { col ->
                        val month = row * 3 + col + 1
                        val selected = year == initialYear && month == initialMonth
                        Box(
                            Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) BrandBlue else Vsoft.colors.surfaceHigh)
                                .clickable { onPick(year, month) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                JCal.monthNames[month - 1],
                                color = if (selected) Color.White else Vsoft.colors.textPrimary,
                                fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            OutlinedButton(onClick = onToday, modifier = Modifier.fillMaxWidth()) { Text("امروز") }
        }
    }
}
