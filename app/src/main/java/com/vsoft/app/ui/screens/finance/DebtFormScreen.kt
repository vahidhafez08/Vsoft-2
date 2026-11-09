package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtFormScreen(onBack: () -> Unit, vm: DebtsViewModel = hiltViewModel()) {
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.I_OWE.name) }
    val valid = person.isNotBlank() && (amount.toLatinDigits().toLongOrNull() ?: 0L) > 0L
    Scaffold(
        containerColor = Vsoft.colors.bg,
        topBar = { TopAppBar(title = { Text("ثبت بدهی یا طلب", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت") } }) },
        bottomBar = {
            Button(onClick = {
                vm.save(person, type, amount.toLatinDigits().toLongOrNull() ?: 0L, null, note)
                onBack()
            }, enabled = valid, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("ذخیره بدهی یا طلب") }
        }
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("اطلاعات اصلی", color = Vsoft.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(person, { person = it.take(60) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("نام شخص یا مجموعه") }, leadingIcon = { Icon(Icons.Default.People, null) })
            Text("نوع حساب", color = Vsoft.colors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(type == DebtType.I_OWE.name, { type = DebtType.I_OWE.name }, label = { Text("من بدهکارم") })
                FilterChip(type == DebtType.OWED_TO_ME.name, { type = DebtType.OWED_TO_ME.name }, label = { Text("از من طلبکارند") })
            }
            OutlinedTextField(amount, { amount = it.toLatinDigits().filter(Char::isDigit).take(15) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("مبلغ (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(note, { note = it.take(250) }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("توضیحات (اختیاری)") })
        }
    }
}
