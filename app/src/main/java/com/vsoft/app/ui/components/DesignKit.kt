package com.vsoft.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft

@Composable
fun VPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, loading: Boolean = false, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    val brush = Brush.horizontalGradient(listOf(BrandBlue, BrandPurple))
    val disabledBrush = Brush.horizontalGradient(listOf(BrandBlue.copy(alpha = 0.4f), BrandPurple.copy(alpha = 0.4f)))
    val shape = RoundedCornerShape(Dimens.cornerPill)
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).clip(shape)
            .background(if (enabled && !loading) brush else disabledBrush)
            .then(if (enabled && !loading) Modifier.bounceClick { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() } else Modifier)
            .padding(horizontal = Dimens.lg, vertical = Dimens.sm),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.size(Dimens.sm))
            }
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun VSecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.cornerPill)
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape)
            .background(Vsoft.colors.surfaceHigh)
            .border(1.dp, Vsoft.colors.outline.copy(alpha = 0.7f), shape)
            .then(if (enabled) Modifier.bounceClick(onClick) else Modifier)
            .padding(horizontal = Dimens.lg, vertical = Dimens.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) Vsoft.colors.textPrimary else Vsoft.colors.textSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun VTextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Vsoft.colors.textSecondary) {
    TextButton(onClick = onClick, modifier = modifier.defaultMinSize(minHeight = Dimens.minTouchTarget)) {
        Text(text, color = color)
    }
}

@Composable
fun VChip(text: String, selected: Boolean, onClick: () -> Unit, leadingIcon: ImageVector? = null, modifier: Modifier = Modifier) {
    val background by animateColorAsState(if (selected) BrandBlue else Vsoft.colors.surfaceHigh, tween(200), label = "chipBackground")
    val foreground by animateColorAsState(if (selected) Color.White else Vsoft.colors.textPrimary, tween(200), label = "chipForeground")
    val shape = RoundedCornerShape(Dimens.cornerPill)
    Row(
        modifier = modifier.defaultMinSize(minHeight = Dimens.minTouchTarget).clip(shape).background(background)
            .border(1.dp, if (selected) Color.Transparent else Vsoft.colors.outline.copy(alpha = 0.65f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.md, vertical = Dimens.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, tint = foreground, modifier = Modifier.size(18.dp))
        Text(text, color = foreground, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
fun VTextField(value: String, onValueChange: (String) -> Unit, label: String? = null, placeholder: String? = null, error: String? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, singleLine: Boolean = true, maxLength: Int? = null, leadingIcon: ImageVector? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { next -> onValueChange(if (maxLength == null) next else next.take(maxLength.coerceAtLeast(0))) },
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue, unfocusedBorderColor = Vsoft.colors.outline, errorBorderColor = ExpenseRed,
                focusedContainerColor = Vsoft.colors.surfaceHigh, unfocusedContainerColor = Vsoft.colors.surfaceHigh,
                focusedTextColor = Vsoft.colors.textPrimary, unfocusedTextColor = Vsoft.colors.textPrimary,
                focusedLabelColor = BrandBlue, unfocusedLabelColor = Vsoft.colors.textSecondary, cursorColor = BrandBlue
            )
        )
        if (error != null) Text(error, color = ExpenseRed, fontSize = 12.sp, modifier = Modifier.padding(top = Dimens.xs, start = Dimens.sm))
    }
}

@Composable
fun VSectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), color = Vsoft.colors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        if (action != null && onAction != null) TextButton(onClick = onAction, modifier = Modifier.defaultMinSize(minHeight = Dimens.minTouchTarget)) { Text(action, color = BrandBlue) }
    }
}

@Composable
fun VListRow(icon: ImageVector, iconTint: Color, title: String, subtitle: String? = null, trailing: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    VCard(modifier = modifier.then(if (onClick != null) Modifier.bounceClick(onClick) else Modifier)) {
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.minTouchTarget), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconTint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Vsoft.colors.textPrimary, fontWeight = FontWeight.Medium)
                if (subtitle != null) Text(subtitle, color = Vsoft.colors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            trailing?.invoke()
        }
    }
}