package com.vsoft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.Vsoft

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .bounceClick(onBack)
                .clip(CircleShape)
                .background(Vsoft.colors.surface)
                .border(1.dp, Vsoft.colors.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Vsoft.colors.textPrimary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Vsoft.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            }
        }
        trailing()
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(Vsoft.colors.surfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Vsoft.colors.textSecondary, modifier = Modifier.size(32.dp))
        }
        Text(title, color = Vsoft.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Vsoft.colors.textSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun FinanceShortcuts(onOpenTransactions: () -> Unit, onOpenAccounts: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ShortcutTile(
            title = "تراکنش‌ها",
            subtitle = "جستجو، فیلتر و ویرایش",
            icon = Icons.Filled.SwapVert,
            tint = BrandBlue,
            modifier = Modifier.weight(1f),
            onClick = onOpenTransactions
        )
        ShortcutTile(
            title = "کارت‌ها و حساب‌ها",
            subtitle = "موجودی و انتقال وجه",
            icon = Icons.Filled.CreditCard,
            tint = BrandPurple,
            modifier = Modifier.weight(1f),
            onClick = onOpenAccounts
        )
    }
}

@Composable
private fun ShortcutTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .bounceClick(onClick)
            .clip(shape)
            .background(Vsoft.colors.surface)
            .border(1.dp, Vsoft.colors.outline, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, color = Vsoft.colors.textSecondary, fontSize = 10.sp, maxLines = 2)
        }
    }
}
