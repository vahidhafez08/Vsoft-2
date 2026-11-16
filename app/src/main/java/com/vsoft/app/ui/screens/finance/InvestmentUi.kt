package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.InvestmentTypeEntity
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormStyle
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.theme.Vsoft

/** Icons the user can pick for a custom asset type. The key is stored in InvestmentTypeEntity.iconKey. */
val InvestmentIconOptions: List<Pair<String, ImageVector>> = listOf(
    "savings" to Icons.Default.Savings,
    "diamond" to Icons.Default.Diamond,
    "paid" to Icons.Default.Paid,
    "land" to Icons.Default.Landscape,
    "apartment" to Icons.Default.Apartment,
    "business" to Icons.Default.Business,
    "watch" to Icons.Default.Watch,
    "art" to Icons.Default.Brush,
    "pets" to Icons.Default.Pets,
    "farm" to Icons.Default.Agriculture,
    "chip" to Icons.Default.Memory,
    "phone" to Icons.Default.PhoneIphone,
    "fuel" to Icons.Default.LocalGasStation,
    "bolt" to Icons.Default.Bolt,
    "box" to Icons.Default.Inventory2,
    "premium" to Icons.Default.WorkspacePremium
)

val InvestmentColorOptions = listOf(
    "#F5B301", "#2E9E5B", "#2F6FDE", "#7B4FE0",
    "#E5484D", "#F7931A", "#00A3A3", "#D6409F"
)

/** Built-in asset keys (seeded in the database) plus the custom icon options. */
fun investmentIconFor(key: String): ImageVector = when (key) {
    "coin" -> Icons.Default.Toll
    "dollar" -> Icons.Default.AttachMoney
    "euro" -> Icons.Default.Euro
    "crypto" -> Icons.Default.CurrencyBitcoin
    "stock" -> Icons.Default.ShowChart
    "fund" -> Icons.Default.PieChart
    "home" -> Icons.Default.Home
    "car" -> Icons.Default.DirectionsCar
    else -> InvestmentIconOptions.firstOrNull { it.first == key }?.second ?: Icons.Default.Category
}

fun parseTypeColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF5C8FE8))

@Composable
private fun GoldBarGlyph(size: Dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val bar = Path().apply {
            moveTo(w * .26f, h * .30f); lineTo(w * .74f, h * .30f)
            lineTo(w * .96f, h * .74f); lineTo(w * .04f, h * .74f); close()
        }
        drawPath(bar, Color.White)
        val shine = Path().apply {
            moveTo(w * .33f, h * .40f); lineTo(w * .67f, h * .40f)
            lineTo(w * .74f, h * .54f); lineTo(w * .26f, h * .54f); close()
        }
        drawPath(shine, Color.Black.copy(alpha = .14f))
    }
}

/** Squircle logo with a soft gradient — one per asset type. */
@Composable
fun InvestmentTypeLogo(iconKey: String, colorHex: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val base = parseTypeColor(colorHex)
    val shape = RoundedCornerShape(size * 0.30f)
    Box(
        modifier.size(size).clip(shape)
            .background(Brush.linearGradient(listOf(lerp(base, Color.White, .30f), base)))
            .border(0.8.dp, Color.White.copy(alpha = .28f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (iconKey == "gold") GoldBarGlyph(size * 0.60f)
        else Icon(investmentIconFor(iconKey), null, tint = Color.White, modifier = Modifier.size(size * 0.52f))
    }
}

/** Minimal pill: tiny logo + name. Used for filtering and for showing an asset's type. */
@Composable
fun InvestmentTypePill(type: InvestmentTypeEntity?, label: String, selected: Boolean, onClick: (() -> Unit)? = null) {
    val c = Vsoft.colors
    val tint = type?.let { parseTypeColor(it.colorHex) } ?: FormStyle.Blue1
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.clip(shape)
            .background(if (selected) tint.copy(alpha = .16f) else c.surfaceHigh)
            .border(1.dp, if (selected) tint.copy(alpha = .75f) else c.outline.copy(alpha = .5f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (type != null) InvestmentTypeLogo(type.iconKey, type.colorHex, 24.dp)
        else Box(Modifier.size(24.dp).clip(CircleShape).background(FormStyle.Blue1.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.GridView, null, tint = FormStyle.IconBlue, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) c.textPrimary else c.textSecondary, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}

/** Selectable tile for the "add investment" form. */
@Composable
fun InvestmentTypeTile(type: InvestmentTypeEntity, selected: Boolean, onClick: () -> Unit) {
    val c = Vsoft.colors
    val tint = parseTypeColor(type.colorHex)
    val shape = RoundedCornerShape(22.dp)
    Column(Modifier.width(76.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(70.dp).clip(shape)
                .background(if (selected) tint.copy(alpha = .14f) else c.surfaceHigh)
                .border(if (selected) 2.dp else 1.dp, if (selected) tint else c.outline.copy(alpha = .5f), shape),
            contentAlignment = Alignment.Center
        ) { InvestmentTypeLogo(type.iconKey, type.colorHex, 46.dp) }
        Spacer(Modifier.height(6.dp))
        Text(type.name, color = if (selected) c.textPrimary else c.textSecondary, fontSize = 12.sp, maxLines = 1, textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
fun InvestmentAddTypeTile(onClick: () -> Unit) {
    val c = Vsoft.colors
    val shape = RoundedCornerShape(22.dp)
    Column(Modifier.width(76.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(70.dp).clip(shape).background(FormStyle.Blue1.copy(alpha = .08f))
                .border(BorderStroke(1.dp, FormStyle.Blue2.copy(alpha = .55f)), shape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Add, null, tint = FormStyle.IconBlue, modifier = Modifier.size(28.dp)) }
        Spacer(Modifier.height(6.dp))
        Text("افزودن", color = c.textSecondary, fontSize = 12.sp, maxLines = 1)
    }
}

/** Bottom sheet to create a custom asset type (name, unit, logo, colour) and to remove custom ones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentTypeSheet(
    existing: List<InvestmentTypeEntity>,
    onSave: (name: String, unit: String, iconKey: String, colorHex: String) -> Unit,
    onDelete: (InvestmentTypeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val c = Vsoft.colors
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf(InvestmentIconOptions.first().first) }
    var colorHex by remember { mutableStateOf(InvestmentColorOptions.first()) }
    val duplicate = existing.any { it.name.trim() == name.trim() }
    val valid = name.isNotBlank() && unit.isNotBlank() && !duplicate
    val custom = existing.filter { !it.isDefault }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InvestmentTypeLogo(iconKey, colorHex, 52.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("نوع سرمایهٔ جدید", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("نام، واحد و لوگوی دلخواه را انتخاب کنید", color = c.textSecondary, fontSize = 12.sp)
                }
            }
            FormTextField(value = name, onValueChange = { name = it.take(24) }, placeholder = "نام دارایی (مثلاً نقره)")
            if (duplicate) Text("این نام قبلاً ثبت شده است", color = Color(0xFFE16B63), fontSize = 12.sp)
            FormTextField(value = unit, onValueChange = { unit = it.take(14) }, placeholder = "واحد (مثلاً گرم، عدد، متر)")

            Text("لوگو", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            InvestmentIconOptions.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { (key, icon) ->
                        val sel = key == iconKey
                        val shape = RoundedCornerShape(16.dp)
                        Box(
                            Modifier.size(56.dp).clip(shape)
                                .background(if (sel) parseTypeColor(colorHex).copy(alpha = .18f) else c.surfaceHigh)
                                .border(if (sel) 2.dp else 1.dp, if (sel) parseTypeColor(colorHex) else c.outline.copy(alpha = .5f), shape)
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) { Icon(icon, null, tint = if (sel) parseTypeColor(colorHex) else c.textSecondary, modifier = Modifier.size(26.dp)) }
                    }
                }
            }

            Text("رنگ", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InvestmentColorOptions.forEach { hex ->
                    val sel = hex == colorHex
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(parseTypeColor(hex))
                            .border(if (sel) 3.dp else 0.dp, c.textPrimary.copy(alpha = .85f), CircleShape)
                            .clickable { colorHex = hex }
                    )
                }
            }

            FormPrimaryButton("افزودن نوع سرمایه", valid, false) {
                onSave(name.trim(), unit.trim(), iconKey, colorHex)
            }

            if (custom.isNotEmpty()) {
                Text("نوع‌های اضافه‌شده توسط شما", color = c.textSecondary, fontSize = 12.sp)
                custom.forEach { type ->
                    val shape = RoundedCornerShape(16.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(c.surfaceHigh).border(1.dp, c.outline.copy(alpha = .5f), shape).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InvestmentTypeLogo(type.iconKey, type.colorHex, 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(type.name, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("واحد: ${type.unitLabel}", color = c.textSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onDelete(type) }) { Icon(Icons.Default.Delete, "حذف", tint = c.textSecondary) }
                    }
                }
            }
        }
    }
}
