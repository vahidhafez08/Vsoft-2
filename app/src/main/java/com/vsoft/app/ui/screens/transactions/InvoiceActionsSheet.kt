package com.vsoft.app.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.components.form.FormStyle
import com.vsoft.app.ui.theme.Vsoft

/** iOS-style sheet shown after an invoice is created: one framed card per format, each with Save / Share. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceActionsSheet(
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit,
    onSaveImage: () -> Unit,
    onShareImage: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = Vsoft.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF279B70).copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF279B70), modifier = Modifier.size(28.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("فاکتور آماده است", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("قالب موردنظر را انتخاب کنید", color = c.textSecondary, fontSize = 13.sp)
            }
            InvoiceFormatCard(Icons.Default.PictureAsPdf, Color(0xFFE5484D), "فایل PDF", "مناسب چاپ و ارسال رسمی", onSavePdf, onSharePdf)
            InvoiceFormatCard(Icons.Default.Image, FormStyle.Blue1, "تصویر PNG", "مناسب ارسال سریع در پیام‌رسان", onSaveImage, onShareImage)
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp)).background(c.surfaceHigh).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) { Text("بستن", color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun InvoiceFormatCard(icon: ImageVector, tint: Color, title: String, subtitle: String, onSave: () -> Unit, onShare: () -> Unit) {
    val c = Vsoft.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(c.surfaceHigh).border(1.dp, c.outline.copy(alpha = .6f), shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = c.textSecondary, fontSize = 12.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InvoiceActionButton("ذخیره", Icons.Default.Download, filled = false, tint = tint, modifier = Modifier.weight(1f), onClick = onSave)
            InvoiceActionButton("اشتراک", Icons.Default.Share, filled = true, tint = tint, modifier = Modifier.weight(1f), onClick = onShare)
        }
    }
}

@Composable
private fun InvoiceActionButton(text: String, icon: ImageVector, filled: Boolean, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.height(46.dp).clip(shape)
            .background(if (filled) tint else tint.copy(alpha = .10f))
            .border(1.dp, if (filled) Color.Transparent else tint.copy(alpha = .45f), shape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (filled) Color.White else tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = if (filled) Color.White else tint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
