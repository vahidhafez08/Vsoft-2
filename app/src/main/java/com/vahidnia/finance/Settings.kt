package com.vahidnia.finance

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Date

@Composable
fun SettingsScreen(vm: AppVm) {
    val c = LocalVC.current
    val ctx = LocalContext.current
    var restoreFile by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("تنظیمات") }
        item {
            Group("ظاهر برنامه") {
                VText("تم", size = 13, color = c.sub)
                ChipRow {
                    Chip("خودکار (دستگاه)", vm.theme == 0) { vm.updateTheme(0) }
                    Chip("روز", vm.theme == 1) { vm.updateTheme(1) }
                    Chip("شب", vm.theme == 2) { vm.updateTheme(2) }
                }
                VText("حالت «خودکار» از تنظیم شب/روز دستگاه پیروی می‌کند.", size = 11, color = c.sub)
                VText("فونت", size = 13, color = c.sub)
                ChipRow {
                    Chip("پیش‌فرض", vm.font == 0) { vm.updateFont(0) }
                    Chip("Sans", vm.font == 1) { vm.updateFont(1) }
                    Chip("Serif", vm.font == 2) { vm.updateFont(2) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText("حالت شیشه‌ای (نوار پایین)", size = 14, weight = FontWeight.Medium)
                    Switch(
                        checked = vm.glass, onCheckedChange = { vm.updateGlass(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    VText("زبان", size = 14, weight = FontWeight.Medium)
                    VText("فارسی", size = 14, color = c.sub)
                }
            }
        }
        item {
            Group("پشتیبان‌گیری و بازیابی") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText("پشتیبان‌گیری خودکار (روزانه)", size = 14, weight = FontWeight.Medium)
                    Switch(
                        checked = vm.autoBackup, onCheckedChange = { vm.updateAuto(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent)
                    )
                }
                VText("تعداد نسخه‌های نگهداری‌شده", size = 13, color = c.sub)
                ChipRow {
                    Chip("${3.fa()} نسخه", vm.keepCount == 3) { vm.updateKeep(3) }
                    Chip("${5.fa()} نسخه", vm.keepCount == 5) { vm.updateKeep(5) }
                    Chip("${10.fa()} نسخه", vm.keepCount == 10) { vm.updateKeep(10) }
                }
                PrimaryButton("پشتیبان‌گیری اکنون", Modifier.fillMaxWidth()) {
                    val ok = vm.backupNow()
                    Toast.makeText(ctx, if (ok) "پشتیبان ذخیره شد" else "پشتیبان‌گیری ناموفق بود", Toast.LENGTH_SHORT).show()
                }
            }
        }
        item {
            Group("نسخه‌های پشتیبان") {
                if (vm.backups.isEmpty()) VText("هنوز نسخه‌ای وجود ندارد.", size = 13, color = c.sub)
                for (f in vm.backups) {
                    val d = Date(f.lastModified())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        VText("${jalali(d.time).text()} · ${timeText(d.time)}", size = 13)
                        Chip("بازیابی", false) { restoreFile = f }
                    }
                }
            }
        }
        item {
            Group("درباره برنامه") {
                VText("Vsoft · مدیریت مالی وحیدینیا", size = 14, weight = FontWeight.Bold)
                VText("نسخه ${"1.0".fa()}", size = 12, color = c.sub)
            }
        }
    }

    restoreFile?.let { f ->
        AppDialog(
            title = "بازیابی اطلاعات؟",
            onDismiss = { restoreFile = null },
            confirmText = "بازیابی",
            danger = true,
            dismissText = "انصراف",
            onConfirm = {
                val ok = vm.restore(f)
                restoreFile = null
                Toast.makeText(ctx, if (ok) "اطلاعات بازیابی شد" else "بازیابی ناموفق بود", Toast.LENGTH_SHORT).show()
            }
        ) {
            VText("اطلاعات فعلی برنامه با این نسخه جایگزین می‌شود. از وضعیت فعلی یک نسخه ایمنی گرفته می‌شود.", size = 14, color = c.sub)
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().vcard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        content()
    }
}
