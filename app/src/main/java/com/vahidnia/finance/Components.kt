package com.vahidnia.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VText(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 14,
    weight: FontWeight = FontWeight.Normal,
    color: Color = LocalVC.current.text,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = LocalFont.current,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun Modifier.vcard(radius: Dp = 20.dp): Modifier {
    val c = LocalVC.current
    val shape = RoundedCornerShape(radius)
    return this.background(c.card, shape).border(1.dp, c.border, shape).clip(shape)
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalVC.current
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) c.accent else c.surfaceVar, shape)
            .border(1.dp, if (selected) c.accent else c.border, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        VText(
            text, size = 13,
            weight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) c.onAccent else c.text
        )
    }
}

@Composable
fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
}

@Composable
fun ProgressBar(progress: Int, color: Color = LocalVC.current.highlight) {
    val c = LocalVC.current
    val p = progress.coerceIn(0, 100) / 100f
    Box(
        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(c.border)
    ) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth(p).clip(RoundedCornerShape(50)).background(color)
        )
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null,
    bg: Color = tint.copy(alpha = 0.16f)
) {
    var m = Modifier.size(size).clip(CircleShape).background(bg)
    if (onClick != null) m = m.clickable { onClick() }
    Box(m, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

fun catIcon(cat: String): ImageVector = when (cat) {
    "خرید" -> Icons.Rounded.ShoppingCart
    "غذا" -> Icons.Rounded.Restaurant
    "قبوض" -> Icons.Rounded.Bolt
    "حمل‌ونقل" -> Icons.Rounded.DirectionsCar
    "اینترنت" -> Icons.Rounded.Wifi
    "مصالح" -> Icons.Rounded.Construction
    "حقوق", "پروژه", "دستمزد" -> Icons.Rounded.Work
    else -> Icons.Rounded.Category
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    VText(text, modifier = modifier, size = 16, weight = FontWeight.Bold)
}

@Composable
fun ScreenTitle(text: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        VText(text, size = 24, weight = FontWeight.ExtraBold)
        trailing()
    }
}

@Composable
fun TxRow(tx: Tx, cardName: String, onClick: () -> Unit) {
    val c = LocalVC.current
    val tint = if (tx.income) c.income else c.expense
    Row(
        Modifier.fillMaxWidth().vcard(16.dp).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(catIcon(tx.category), tint, bg = if (tx.income) c.incomeBg else c.expenseBg)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            VText(if (tx.note.isBlank()) tx.category else tx.note, size = 14, weight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            VText("${cardName} · ${timeText(tx.time)}", size = 11, color = c.sub, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (tx.income) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = null, tint = tint, modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(2.dp))
                VText(tx.amount.money(), size = 14, weight = FontWeight.Bold, color = tint)
            }
            VText(if (tx.income) "درآمد" else "هزینه", size = 11, color = c.sub)
        }
    }
}

@Composable
fun BankCardView(card: BankCard, balance: Long, selected: Boolean, width: Dp = 250.dp, onClick: () -> Unit) {
    val colors = bankColors(card.bank, LocalVC.current.dark)
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .width(width)
            .height(140.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors[1], colors[0])), shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.18f), shape)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            VText(card.bank, size = 15, weight = FontWeight.Bold, color = Color.White)
            if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = Color.White, modifier = Modifier.size(22.dp))
            else Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
        }
        VText("کارت ${card.last4.fa()}", size = 13, color = Color.White.copy(alpha = 0.8f))
        Row(verticalAlignment = Alignment.Bottom) {
            VText(balance.money(), size = 22, weight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.width(6.dp))
            VText("تومان", size = 12, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun EmptyBox(title: String, body: String, actions: @Composable () -> Unit = {}) {
    val c = LocalVC.current
    Column(
        Modifier.fillMaxWidth().vcard().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VText(title, size = 16, weight = FontWeight.Bold, align = TextAlign.Center)
        VText(body, size = 13, color = c.sub, align = TextAlign.Center)
        actions()
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalVC.current
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.accent)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { VText(text, size = 14, weight = FontWeight.Bold, color = c.onAccent) }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalVC.current
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .clip(shape)
            .background(c.surfaceVar)
            .border(1.dp, c.border, shape)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { VText(text, size = 14, weight = FontWeight.Bold, color = c.text) }
}

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    number: Boolean = false,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    val c = LocalVC.current
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { VText(label, size = 12, color = c.sub) },
        singleLine = singleLine,
        keyboardOptions = if (number) KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) else KeyboardOptions.Default,
        textStyle = TextStyle(color = c.text, fontSize = 15.sp, fontFamily = LocalFont.current),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.accent,
            unfocusedBorderColor = c.border,
            focusedTextColor = c.text,
            unfocusedTextColor = c.text,
            cursorColor = c.accent
        )
    )
}

@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    dismissText: String = "بستن",
    danger: Boolean = false,
    content: @Composable () -> Unit
) {
    val c = LocalVC.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.dialog,
        shape = RoundedCornerShape(24.dp),
        title = { VText(title, size = 18, weight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        },
        confirmButton = {
            if (confirmText != null) {
                TextButton(onClick = onConfirm) {
                    VText(confirmText, size = 14, weight = FontWeight.Bold, color = if (danger) c.expense else c.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { VText(dismissText, size = 14, color = c.sub) }
        }
    )
}

@Composable
fun BarChart(groups: List<Pair<Long, Long>>, labels: List<String>) {
    val c = LocalVC.current
    val maxV = (groups.maxOfOrNull { maxOf(it.first, it.second) } ?: 0L).coerceAtLeast(1L)
    val inc = c.income
    val exp = c.expense
    val grid = c.divider
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val w = size.width
        val h = size.height
        for (i in 0..3) {
            val y = h * i / 3f
            drawLine(grid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        val n = groups.size.coerceAtLeast(1)
        val slot = w / n
        val bw = slot * 0.3f
        for (i in groups.indices) {
            // در RTL اولین گروه سمت راست قرار می‌گیرد
            val x0 = w - slot * (i + 1) + slot * 0.2f
            val hi = h * (groups[i].first.toFloat() / maxV)
            val he = h * (groups[i].second.toFloat() / maxV)
            drawRect(inc, Offset(x0, h - hi), Size(bw, hi))
            drawRect(exp, Offset(x0 + bw + slot * 0.05f, h - he), Size(bw, he))
        }
    }
    Spacer(Modifier.height(6.dp))
    if (labels.size <= 12) {
        Row(Modifier.fillMaxWidth()) {
            for (l in labels) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    VText(l, size = 10, color = c.sub, maxLines = 1, align = TextAlign.Center)
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            VText(labels.first(), size = 10, color = c.sub)
            VText(labels[labels.size / 2], size = 10, color = c.sub)
            VText(labels.last(), size = 10, color = c.sub)
        }
    }
}

@Composable
fun Donut(values: List<Pair<Float, Color>>, size: Dp = 120.dp, center: @Composable () -> Unit) {
    val total = values.sumOf { it.first.toDouble() }.toFloat()
    val stroke = remember { 18f }
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val pad = stroke
            val arcSize = Size(this.size.width - pad * 2, this.size.height - pad * 2)
            var start = -90f
            if (total <= 0f) return@Canvas
            for (v in values) {
                val sweep = 360f * (v.first / total)
                drawArc(
                    color = v.second, startAngle = start, sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                    useCenter = false, topLeft = Offset(pad, pad), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        center()
    }
}

@Composable
fun NavIcon(page: Int, selected: Boolean): ImageVector = when (page) {
    0 -> Icons.Rounded.Home
    1 -> Icons.Rounded.AccountBalanceWallet
    2 -> Icons.Rounded.Work
    3 -> Icons.Rounded.BarChart
    else -> Icons.Rounded.Settings
}

@Composable
fun BottomBar(current: Int, onSelect: (Int) -> Unit) {
    val c = LocalVC.current
    val titles = listOf("خانه", "امور مالی", "کار و پروژه‌ها", "گزارش‌ها", "تنظیمات")
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (c.glass) c.bar.copy(alpha = 0.94f) else c.bar)
            .border(BorderStroke(1.dp, c.divider))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (i in titles.indices) {
            val sel = i == current
            Column(
                Modifier.weight(1f).clickable { onSelect(i) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(NavIcon(i, sel), null, tint = if (sel) c.accent else c.sub, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(2.dp))
                VText(titles[i], size = 10, weight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) c.accent else c.sub, maxLines = 1)
            }
        }
    }
}

@Composable
fun StatusPill(status: Status) {
    val sc = LocalVC.current.statusColors(status)
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(sc.bg).padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) { VText(sc.label, size = 11, weight = FontWeight.Bold, color = sc.fg) }
}
