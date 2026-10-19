package com.vsoft.app.ui.components.form

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toPersianDigits

@Composable
fun FormChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Vsoft.colors
    val bg by animateColorAsState(if (selected) FormStyle.Blue1 else c.surfaceHigh, tween(200), label = "formChipBg")
    val fg by animateColorAsState(if (selected) Color.White else c.textPrimary, tween(200), label = "formChipFg")
    Row(modifier.padding(2.dp).heightIn(min = 44.dp).clip(RoundedCornerShape(20.dp))
        .background(if (selected) FormStyle.gradient else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(bg, bg)))
        .border(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else c.outline.copy(alpha = .6f), RoundedCornerShape(20.dp))
        .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun FormSwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = checked, onCheckedChange = onChange)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) Text(subtitle, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
        }
    }
}

data class FormOption(val id: Long?, val label: String, val sub: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormOptionSheet(title: String, options: List<FormOption>, selectedId: Long?, onPick: (Long?) -> Unit, onDismiss: () -> Unit) {
    val c = Vsoft.colors
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface) {
        Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            options.forEach { option ->
                val selected = option.id == selectedId
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(FormStyle.fieldShape)
                    .background(c.surfaceHigh).border(if (selected) 1.5.dp else 1.dp, if (selected) FormStyle.Blue2 else c.outline.copy(alpha = .5f), FormStyle.fieldShape)
                    .clickable { onPick(option.id) }.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(option.label, color = c.textPrimary, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        option.sub?.let { Text(it, color = c.textSecondary, fontSize = 12.sp) }
                    }
                    if (selected) Text("✓", color = FormStyle.Blue2, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun FormAmountField(rawDigits: String, onChange: (String) -> Unit, placeholder: String, unit: String = "تومان", maxDigits: Int = 15) {
    FormTextField(value = FormFormat.groupDigits(rawDigits.toLatinDigits().filter(Char::isDigit).take(maxDigits)).replace(",", "٬"),
        onValueChange = { incoming -> onChange(incoming.toLatinDigits().filter(Char::isDigit).take(maxDigits)) },
        placeholder = placeholder, keyboardType = KeyboardType.Number,
        trailing = { Text(unit, color = Vsoft.colors.textSecondary, fontSize = 12.sp) })
}

@Composable
fun FormNumberField(raw: String, onChange: (String) -> Unit, placeholder: String, unit: String?, maxDigits: Int = 9, allowDecimal: Boolean = true) {
    val normalized = raw.toLatinDigits()
    val display = normalized.toPersianDigits()
    FormTextField(value = display, onValueChange = { input ->
        val latin = input.toLatinDigits()
        var dotSeen = false
        val clean = buildString {
            latin.forEach { ch ->
                if (ch.isDigit() && count { it.isDigit() } < maxDigits) append(ch)
                else if (allowDecimal && ch == '.' && !dotSeen) { append(ch); dotSeen = true }
            }
        }
        onChange(clean)
    }, placeholder = placeholder, keyboardType = KeyboardType.Decimal,
        trailing = if (unit.isNullOrBlank()) null else ({ Text(unit, color = Vsoft.colors.textSecondary, fontSize = 12.sp) }))
}

@Composable
fun FormCounterText(current: Int, max: Int) {
    Text("${current.coerceAtMost(max).toString().toPersianDigits()}/${max.toString().toPersianDigits()}",
        color = Vsoft.colors.textSecondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
}
