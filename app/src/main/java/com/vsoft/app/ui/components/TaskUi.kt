package com.vsoft.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.WarningOrange

fun priorityLabel(p: Int): String = when (p) {
    2 -> "اولویت بالا"
    1 -> "اولویت متوسط"
    else -> "اولویت کم"
}

fun priorityColor(p: Int): Color = when (p) {
    2 -> ExpenseRed
    1 -> WarningOrange
    else -> IncomeGreen
}

@Composable
fun AnimatedProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Box(
        modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(Vsoft.colors.outline)
    ) {
        Box(Modifier.fillMaxWidth(animated).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
    }
}

@Composable
fun TaskCheck(done: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = IncomeGreen) {
    val fill by animateColorAsState(if (done) color else Color.Transparent, label = "checkFill")
    val borderColor by animateColorAsState(if (done) color else Vsoft.colors.textSecondary, label = "checkBorder")
    val checkScale by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale"
    )
    Box(modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(fill).border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp).scale(checkScale))
        }
    }
}

@Composable
fun MiniChip(text: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun TasksFab(onNewTask: () -> Unit, onNewProject: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "tasksFabRotation")
    Column(modifier, horizontalAlignment = Alignment.Start) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + scaleIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + scaleOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FabMini("پروژه جدید", Icons.Filled.CreateNewFolder, BrandPurple) { expanded = false; onNewProject() }
                FabMini("کار جدید", Icons.Filled.TaskAlt, BrandBlue) { expanded = false; onNewTask() }
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = BrandBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "افزودن", modifier = Modifier.rotate(rotation))
        }
    }
}

@Composable
private fun FabMini(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(46.dp).bounceClick(onClick).clip(CircleShape).background(tint),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = label, tint = Color.White) }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Vsoft.colors.surfaceHigh).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(label, color = Vsoft.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Shared bottom-sheet shell: keyboard/nav-bar aware, scrollable column. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
internal fun SheetTitle(text: String) =
    Text(text, color = Vsoft.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
internal fun SheetLabel(text: String) =
    Text(text, color = Vsoft.colors.textSecondary, fontSize = 13.sp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChipGroup(labels: List<String>, isSelected: (Int) -> Boolean, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { i, l ->
            FilterChip(selected = isSelected(i), onClick = { onSelect(i) }, label = { Text(l) })
        }
    }
}

@Composable
fun SingleInputSheet(title: String, label: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    FormSheet(onDismiss) {
        SheetTitle(title)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(60) },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(confirmLabel)
        }
    }
}
