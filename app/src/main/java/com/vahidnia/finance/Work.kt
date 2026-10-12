package com.vahidnia.finance

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WorkScreen(vm: AppVm, openId: String?, setOpen: (String?) -> Unit) {
    val place = vm.places.firstOrNull { it.id == openId }
    BackHandler(enabled = place != null) { setOpen(null) }
    if (place == null) WorkList(vm, setOpen) else WorkDetail(vm, place) { setOpen(null) }
}

@Composable
private fun WorkList(vm: AppVm, setOpen: (String?) -> Unit) {
    val c = LocalVC.current
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("کار و پروژه‌ها") }
        item { SectionTitle("مکان‌های کاری") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.places, key = { it.id }) { p ->
                    Column(
                        Modifier.width(240.dp).vcard().clickable { setOpen(p.id) }.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Rounded.Construction, c.accent)
                            Spacer(Modifier.width(10.dp))
                            VText(p.name, size = 15, weight = FontWeight.Bold, maxLines = 2)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            VText("پیشرفت پروژه", size = 12, color = c.sub)
                            VText("${p.progress().fa()}٪", size = 13, weight = FontWeight.Bold, color = c.accent)
                        }
                        ProgressBar(p.progress())
                        VText("${p.totalHours().fa()} ساعت کاری · ${p.items.size.fa()} فعالیت", size = 11, color = c.sub)
                    }
                }
                item {
                    Box(
                        Modifier.width(140.dp).height(150.dp).vcard().clickable { showAdd = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconBadge(Icons.Rounded.Add, c.accent, 40.dp)
                            VText("مکان جدید", size = 12, color = c.sub)
                        }
                    }
                }
            }
        }
        if (vm.places.isEmpty()) {
            item {
                EmptyBox("مکان کاری ثبت نشده", "یک مکان کاری یا پروژه اضافه کن تا فعالیت‌های ساختمانی را ثبت کنی.") {
                    PrimaryButton("افزودن مکان کاری") { showAdd = true }
                }
            }
        } else {
            item {
                Column(Modifier.fillMaxWidth().vcard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("جمع کل")
                    val hours = vm.places.sumOf { it.totalHours() }
                    val days = vm.places.sumOf { it.totalDays() }
                    VText("${vm.places.size.fa()} مکان کاری · ${hours.fa()} ساعت کاری · ${days.fa()} روز کاری", size = 13, color = c.sub)
                }
            }
        }
    }

    if (showAdd) {
        AppDialog(
            title = "مکان کاری جدید",
            onDismiss = { showAdd = false; name = "" },
            confirmText = "افزودن",
            dismissText = "انصراف",
            onConfirm = {
                if (name.isNotBlank()) {
                    vm.addPlace(name.trim())
                    name = ""
                    showAdd = false
                }
            }
        ) { Field(name, { name = it }, "نام مکان یا پروژه") }
    }
}

@Composable
private fun WorkDetail(vm: AppVm, place: Place, onBack: () -> Unit) {
    val c = LocalVC.current
    var editing by remember { mutableStateOf<WorkItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf<WorkItem?>(null) }
    var deletePlace by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.ArrowForward, null, tint = c.text, modifier = Modifier.size(26.dp).clickable { onBack() })
                    Spacer(Modifier.width(10.dp))
                    VText(place.name, size = 20, weight = FontWeight.ExtraBold, maxLines = 1)
                }
                Icon(Icons.Rounded.Delete, null, tint = c.expense, modifier = Modifier.size(24.dp).clickable { deletePlace = true })
            }
        }
        item {
            Column(Modifier.fillMaxWidth().vcard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText("پیشرفت پروژه", size = 14, color = c.sub)
                    VText("${place.progress().fa()}٪", size = 22, weight = FontWeight.ExtraBold, color = c.accent)
                }
                ProgressBar(place.progress())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat("ساعت کاری", place.totalHours().fa(), Modifier.weight(1f))
                    Stat("روز کاری", place.totalDays().fa(), Modifier.weight(1f))
                    Stat("فعالیت", place.items.size.fa(), Modifier.weight(1f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("فعالیت‌ها")
                PrimaryButton("فعالیت جدید") { creating = true }
            }
        }
        if (place.items.isEmpty()) {
            item { EmptyBox("فعالیتی ثبت نشده", "با دکمه «فعالیت جدید» اولین کار را ثبت کن.") }
        }
        items(place.items, key = { it.id }) { w ->
            Column(
                Modifier.fillMaxWidth().vcard().clickable { editing = w }.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText(w.title, size = 15, weight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 2)
                    VText("${w.progress.fa()}٪", size = 14, weight = FontWeight.Bold, color = c.accent)
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Rounded.Delete, null, tint = c.sub, modifier = Modifier.size(20.dp).clickable { deleteItem = w })
                }
                ProgressBar(w.progress)
                val floor = if (w.floor == 0) "همکف" else "طبقه ${w.floor.fa()}"
                VText("$floor · ${w.days.fa()} روز کاری · ${w.hours.fa()} ساعت", size = 12, color = c.sub)
                VText(workerText(w), size = 12, color = c.sub)
            }
        }
    }

    if (creating || editing != null) {
        ItemDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { vm.saveItem(place.id, it); creating = false; editing = null },
            newId = vm.newItemId()
        )
    }
    deleteItem?.let { w ->
        AppDialog("حذف فعالیت؟", { deleteItem = null }, "حذف", { vm.deleteItem(place.id, w.id); deleteItem = null }, "انصراف", true) {
            VText("«${w.title}» حذف می‌شود.", size = 14, color = c.sub)
        }
    }
    if (deletePlace) {
        AppDialog("حذف مکان کاری؟", { deletePlace = false }, "حذف", { vm.deletePlace(place.id); deletePlace = false; onBack() }, "انصراف", true) {
            VText("«${place.name}» و همه فعالیت‌های آن حذف می‌شود.", size = 14, color = c.sub)
        }
    }
}

fun workerText(w: WorkItem): String = when (w.worker) {
    0 -> "انجام‌دهنده: خودم"
    1 -> if (w.workerName.isBlank()) "انجام‌دهنده: با همکاران" else "انجام‌دهنده: با همکاران (${w.workerName})"
    else -> if (w.workerName.isBlank()) "انجام‌دهنده: شخص دیگر" else "انجام‌دهنده: ${w.workerName}"
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    val c = LocalVC.current
    Column(
        modifier.border(1.dp, c.border, RoundedCornerShape(14.dp)).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VText(value, size = 18, weight = FontWeight.ExtraBold)
        VText(label, size = 11, color = c.sub)
    }
}

@Composable
private fun ItemDialog(initial: WorkItem?, newId: String, onDismiss: () -> Unit, onSave: (WorkItem) -> Unit) {
    val c = LocalVC.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var progress by remember { mutableStateOf((initial?.progress ?: 0).toFloat()) }
    var floor by remember { mutableStateOf(initial?.floor?.toString() ?: "") }
    var days by remember { mutableStateOf(initial?.days?.toString() ?: "") }
    var hours by remember { mutableStateOf(initial?.hours?.toString() ?: "") }
    var worker by remember { mutableStateOf(initial?.worker ?: 0) }
    var workerName by remember { mutableStateOf(initial?.workerName ?: "") }

    AppDialog(
        title = if (initial == null) "فعالیت جدید" else "ویرایش فعالیت",
        onDismiss = onDismiss,
        confirmText = "ذخیره",
        dismissText = "انصراف",
        onConfirm = {
            if (title.isNotBlank()) {
                onSave(
                    WorkItem(
                        initial?.id ?: newId, title.trim(), progress.toInt().coerceIn(0, 100),
                        parseAmount(floor).toInt(), parseAmount(days).toInt(), parseAmount(hours).toInt(),
                        worker, if (worker == 0) "" else workerName.trim()
                    )
                )
            }
        }
    ) {
        Field(title, { title = it }, "عنوان فعالیت")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            VText("درصد پیشرفت", size = 13, color = c.sub)
            VText("${progress.toInt().fa()}٪", size = 13, weight = FontWeight.Bold, color = c.accent)
        }
        Slider(
            value = progress, onValueChange = { progress = it }, valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.border)
        )
        Field(floor, { floor = it }, "شماره طبقه (۰ = همکف)", number = true)
        Field(days, { days = it }, "تعداد روز کاری", number = true)
        Field(hours, { hours = it }, "مجموع ساعت کاری", number = true)
        VText("انجام‌دهنده", size = 13, color = c.sub)
        ChipRow {
            Chip("خودم", worker == 0) { worker = 0 }
            Chip("با همکاران", worker == 1) { worker = 1 }
            Chip("شخص دیگر", worker == 2) { worker = 2 }
        }
        if (worker != 0) Field(workerName, { workerName = it }, if (worker == 1) "نام همکاران" else "نام شخص")
    }
}
