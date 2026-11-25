package com.vsoft.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormStyle
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.theme.Vsoft

@Composable
fun GradientLogo(color: Color, icon: ImageVector, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * .30f)
    Box(modifier.size(size).clip(shape).background(Brush.linearGradient(listOf(lerp(color, Color.White, .28f), color)))
        .border(.8.dp, Color.White.copy(alpha = .25f), shape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * .52f))
    }
}
@Composable
fun CategoryLogo(name: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val st = CategoryStyles.of(name); GradientLogo(st.color, st.icon, size, modifier)
}
@Composable
fun AccountLogo(colorKey: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * .30f)
    Box(modifier.size(size).clip(shape).background(accountBrush(colorKey))
        .border(.8.dp, Color.White.copy(alpha = .25f), shape), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(size * .5f))
    }
}
@Composable
fun CategoryTile(name: String, selected: Boolean, onClick: () -> Unit) {
    val c = Vsoft.colors; val tint = CategoryStyles.of(name).color; val shape = RoundedCornerShape(22.dp)
    Column(Modifier.width(76.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(70.dp).clip(shape).background(if (selected) tint.copy(alpha = .14f) else c.surfaceHigh)
            .border(if (selected) 2.dp else 1.dp, if (selected) tint else c.outline.copy(alpha = .5f), shape),
            contentAlignment = Alignment.Center) { CategoryLogo(name, 46.dp) }
        Spacer(Modifier.height(6.dp))
        Text(name, color = if (selected) c.textPrimary else c.textSecondary, fontSize = 11.sp, maxLines = 2,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
@Composable
fun CategoryAddTile(onClick: () -> Unit) {
    val c = Vsoft.colors; val shape = RoundedCornerShape(22.dp)
    Column(Modifier.width(76.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(70.dp).clip(shape).background(FormStyle.Blue1.copy(alpha = .08f))
            .border(BorderStroke(1.dp, FormStyle.Blue2.copy(alpha = .55f)), shape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, null, tint = FormStyle.IconBlue, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp)); Text("افزودن", color = c.textSecondary, fontSize = 11.sp, maxLines = 1)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(isIncome: Boolean, onAdded: (String) -> Unit, onDeleted: (String) -> Unit, onDismiss: () -> Unit) {
    val c = Vsoft.colors; val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf(CategoryIconOptions.first().first) }
    var colorHex by remember { mutableStateOf(CategoryColorOptions.first()) }
    val tint = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFF64748B))
    val duplicate = name.isNotBlank() && CustomCategories.exists(name)
    val valid = name.isNotBlank() && !duplicate
    val custom = CustomCategories.items.filter { it.isIncome == isIncome }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            .navigationBarsPadding().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientLogo(tint, categoryIconFor(iconKey), 52.dp); Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (isIncome) "دستهٔ جدید درآمد" else "دستهٔ جدید هزینه", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("نام، لوگو و رنگ دلخواه را انتخاب کنید", color = c.textSecondary, fontSize = 12.sp)
                }
            }
            FormTextField(value = name, onValueChange = { name = it.take(20) }, placeholder = "نام دسته‌بندی (مثلاً مصالح برقی)")
            if (duplicate) Text("این نام قبلاً وجود دارد", color = Color(0xFFE16B63), fontSize = 12.sp)
            Text("لوگو", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            CategoryIconOptions.chunked(6).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { (key, icon) ->
                        val sel = key == iconKey; val shape = RoundedCornerShape(14.dp)
                        Box(Modifier.size(46.dp).clip(shape).background(if (sel) tint.copy(alpha = .18f) else c.surfaceHigh)
                            .border(if (sel) 2.dp else 1.dp, if (sel) tint else c.outline.copy(alpha = .5f), shape)
                            .clickable { iconKey = key }, contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = if (sel) tint else c.textSecondary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            Text("رنگ", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CategoryColorOptions.forEach { hex ->
                    val sel = hex == colorHex
                    val col = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                    Box(Modifier.size(28.dp).clip(CircleShape).background(col)
                        .border(if (sel) 3.dp else 0.dp, c.textPrimary.copy(alpha = .85f), CircleShape).clickable { colorHex = hex })
                }
            }
            FormPrimaryButton("افزودن دسته‌بندی", valid, false) {
                CustomCategories.add(context, name, iconKey, colorHex, isIncome)?.let(onAdded)
            }
            if (custom.isNotEmpty()) {
                Text("دسته‌های اضافه‌شده توسط شما", color = c.textSecondary, fontSize = 12.sp)
                custom.forEach { cat ->
                    val shape = RoundedCornerShape(16.dp)
                    Row(Modifier.fillMaxWidth().clip(shape).background(c.surfaceHigh)
                        .border(1.dp, c.outline.copy(alpha = .5f), shape).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CategoryLogo(cat.name, 36.dp); Spacer(Modifier.width(10.dp))
                        Text(cat.name, Modifier.weight(1f), color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { CustomCategories.remove(context, cat.name); onDeleted(cat.name) }) {
                            Icon(Icons.Default.Delete, "حذف", tint = c.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
