package com.vsoft.app.ui.screens.more

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.WarningOrange
import com.vsoft.app.util.JCal
import com.vsoft.app.util.toPersianDigits
import java.util.Locale

@Composable
fun MoreScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // 0 = none, 1 = edit name, 2 = privacy, 3 = about, 4 = logout confirm, 5 = restore confirm
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }
    val backupFileName = remember {
        val date = JCal.toJalali(System.currentTimeMillis())
        "vsoft-backup-%04d-%02d-%02d.json".format(Locale.US, date[0], date[1], date[2])
    }
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.backupTo(uri)
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            dialog = 5
        }
    }
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) viewModel.exportCsvTo(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("بیشتر", color = Vsoft.colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // Profile
        VCard {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { dialog = 1 },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.trim().firstOrNull()?.toString() ?: "؟",
                        color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(name, color = Vsoft.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("برای ویرایش نام لمس کنید", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش نام", tint = Vsoft.colors.textSecondary)
            }
        }

        // Theme
        VCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Filled.Palette, BrandPurple)
                Spacer(Modifier.width(12.dp))
                Text("تم برنامه", color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            SegmentedTabs(
                items = listOf("سیستم", "روشن", "تیره"),
                selected = themeMode,
                onSelect = { viewModel.setThemeMode(it) }
            )
        }

        // General
        VCard {
            SettingsRow(Icons.Filled.Language, BrandBlue, "زبان", onClick = {
                Toast.makeText(context, "فعلاً فقط زبان فارسی پشتیبانی می‌شود", Toast.LENGTH_SHORT).show()
            })
            RowDivider()
            SettingsRow(
                Icons.Filled.Notifications, WarningOrange, "اعلان‌ها",
                onClick = { viewModel.setNotifications(!notifications) },
                trailing = {
                    Switch(checked = notifications, onCheckedChange = { viewModel.setNotifications(it) })
                }
            )
        }

        Text(
            "داده‌ها و پشتیبان‌گیری",
            color = Vsoft.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        VCard {
            SettingsRow(
                Icons.Filled.CloudUpload, BrandBlue, "گرفتن بکاپ",
                subtitle = "ذخیره‌ی همه‌ی اطلاعات در یک فایل",
                enabled = !busy,
                onClick = { backupLauncher.launch(backupFileName) }
            )
            RowDivider()
            SettingsRow(
                Icons.Filled.CloudDownload, IncomeGreen, "بازیابی از بکاپ",
                subtitle = "جایگزینی اطلاعات فعلی با فایل بکاپ",
                enabled = !busy,
                onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")) }
            )
            RowDivider()
            SettingsRow(
                Icons.Filled.FileDownload, BrandPurple, "خروجی CSV تراکنش‌ها",
                subtitle = "برای باز کردن در اکسل",
                enabled = !busy,
                onClick = { csvLauncher.launch("vsoft-transactions.csv") }
            )
            if (busy) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BrandBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("در حال انجام...", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                }
            }
        }

        // Privacy and about
        VCard {
            SettingsRow(Icons.Filled.PrivacyTip, IncomeGreen, "حریم خصوصی", onClick = { dialog = 2 })
            RowDivider()
            SettingsRow(Icons.Filled.Info, BrandBlue, "درباره VSOFT", onClick = { dialog = 3 })
        }

        // Logout
        VCard {
            SettingsRow(Icons.Filled.Logout, ExpenseRed, "خروج", onClick = { dialog = 4 }, titleColor = ExpenseRed)
        }
    }

    when (dialog) {
        1 -> NameDialog(
            current = name,
            onDismiss = { dialog = 0 },
            onSave = {
                viewModel.setUserName(it)
                dialog = 0
            }
        )
        2 -> InfoDialog(
            title = "حریم خصوصی",
            text = "همه‌ی اطلاعات شما (تراکنش‌ها، کارها و تنظیمات) فقط روی همین گوشی ذخیره می‌شود و به هیچ سرویسی ارسال نمی‌شود.",
            onDismiss = { dialog = 0 }
        )
        3 -> InfoDialog(
            title = "درباره VSOFT",
            text = "VSOFT مدیریت مالی و کارهای شخصی\nنسخه " + versionName.toPersianDigits(),
            onDismiss = { dialog = 0 }
        )
        4 -> AlertDialog(
            onDismissRequest = { dialog = 0 },
            containerColor = Vsoft.colors.surface,
            title = { Text("خروج", color = Vsoft.colors.textPrimary) },
            text = {
                Text(
                    "اپ فعلاً حساب کاربری ندارد. این گزینه فقط نام، تم و اعلان‌ها را به حالت اولیه برمی‌گرداند. تراکنش‌ها و کارهای شما پاک نمی‌شوند.",
                    color = Vsoft.colors.textPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetSettings()
                    dialog = 0
                }) { Text("بازنشانی", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("انصراف") } }
        )
        5 -> AlertDialog(
            onDismissRequest = {
                pendingRestoreUri = null
                dialog = 0
            },
            containerColor = Vsoft.colors.surface,
            title = { Text("بازیابی اطلاعات", color = Vsoft.colors.textPrimary) },
            text = {
                Text(
                    "همه‌ی اطلاعات فعلی برنامه حذف و با اطلاعات فایل بکاپ جایگزین می‌شه. این کار قابل بازگشت نیست. ادامه می‌دی؟",
                    color = Vsoft.colors.textPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri?.let(viewModel::restoreFrom)
                    pendingRestoreUri = null
                    dialog = 0
                }) { Text("بازیابی", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingRestoreUri = null
                    dialog = 0
                }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
    Box(
        Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    titleColor: Color = Color.Unspecified,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, tint)
        Spacer(Modifier.width(12.dp))
        if (subtitle != null) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    color = if (titleColor == Color.Unspecified) Vsoft.colors.textPrimary else titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(subtitle, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            }
        } else {
            Text(
                title,
                color = if (titleColor == Color.Unspecified) Vsoft.colors.textPrimary else titleColor,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
        if (value != null) {
            Text(value, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(8.dp))
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Vsoft.colors.textSecondary)
        }
    }
}

@Composable
private fun RowDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Vsoft.colors.surface,
        title = { Text(title, color = Vsoft.colors.textPrimary) },
        text = { Text(text, color = Vsoft.colors.textPrimary) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("باشه") } }
    )
}

@Composable
private fun NameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(current) }
    val valid = text.trim().isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Vsoft.colors.surface,
        title = { Text("نام شما", color = Vsoft.colors.textPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(24) },
                singleLine = true,
                isError = !valid,
                label = { Text("نام") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (valid) onSave(text.trim()) }, enabled = valid) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
