package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.DebtEntity
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtsViewModel @Inject constructor(private val repo: FinanceRepository) : ViewModel() {
    val debts = repo.observeDebts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun save(name:String,type:String,amount:Long,due:Long?,note:String) = viewModelScope.launch {
        if (name.isBlank() || amount <= 0L) return@launch
        repo.saveDebt(DebtEntity(personName=name.trim(),type=type,totalAmount=amount,dateMillis=System.currentTimeMillis(),dueDateMillis=due,note=note.trim()))
    }
    fun settle(debt:DebtEntity, settled:Boolean) = viewModelScope.launch { repo.setDebtManualSettlement(debt,settled) }
    fun delete(debt:DebtEntity) = viewModelScope.launch { repo.deleteDebt(debt) }
    suspend fun paid(id:Long) = repo.debtPaid(id)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(onBack:()->Unit, vm:DebtsViewModel = hiltViewModel()) {
    val debts by vm.debts.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<DebtEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.I_OWE.name) }
    var paid by remember { mutableStateOf<Map<Long,Long>>(emptyMap()) }
    LaunchedEffect(debts) { paid = debts.associate { it.id to vm.paid(it.id) } }
    val filtered = debts.filter { it.type == if (tab == 0) DebtType.I_OWE.name else DebtType.OWED_TO_ME.name }
    val totalOwed = debts.filter { it.type==DebtType.I_OWE.name && !it.isSettled }.sumOf { (it.totalAmount-(paid[it.id]?:0L)).coerceAtLeast(0L) }
    val totalToMe = debts.filter { it.type==DebtType.OWED_TO_ME.name && !it.isSettled }.sumOf { (it.totalAmount-(paid[it.id]?:0L)).coerceAtLeast(0L) }
    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Text("بدهی‌ها و طلب‌ها", color=Vsoft.colors.textPrimary, fontSize=21.sp)
            IconButton(onClick=onBack) { Icon(Icons.Default.ArrowBack,null) }
        }
        Card(colors=CardDefaults.cardColors(containerColor=Vsoft.colors.surface), modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("مجموع بدهی من: ${totalOwed.toMoney()} تومان", color=Vsoft.colors.textPrimary)
                Text("مجموع طلب من: ${totalToMe.toMoney()} تومان", color=Vsoft.colors.textPrimary)
            }
        }
        Spacer(Modifier.height(10.dp))
        TabRow(selectedTabIndex=tab) {
            Tab(selected=tab==0,onClick={tab=0},text={Text("بدهکارم")})
            Tab(selected=tab==1,onClick={tab=1},text={Text("طلبکارم")})
        }
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=12.dp)) {
            items(filtered,key={it.id}) { debt ->
                val p=paid[debt.id]?:0L
                val remaining=(debt.totalAmount-p).coerceAtLeast(0L)
                Card(colors=CardDefaults.cardColors(containerColor=Vsoft.colors.surface)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                            Text(debt.personName,color=Vsoft.colors.textPrimary,fontSize=16.sp)
                            Text(if(debt.isSettled)"تسویه‌شده" else "باز",color=Vsoft.colors.textSecondary)
                        }
                        Text("کل: ${debt.totalAmount.toMoney()} · پرداخت/دریافت: ${p.toMoney()} · مانده: ${remaining.toMoney()} تومان",color=Vsoft.colors.textSecondary,fontSize=12.sp)
                        LinearProgressIndicator(progress=if(debt.totalAmount>0)(p.toFloat()/debt.totalAmount).coerceIn(0f,1f) else 0f,modifier=Modifier.fillMaxWidth().padding(vertical=8.dp))
                        if(debt.note.isNotBlank()) Text(debt.note,color=Vsoft.colors.textSecondary,fontSize=12.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick={vm.settle(debt,!debt.isSettled)}) { Text(if(debt.isSettled)"لغو تسویه" else "تسویه دستی") }
                            TextButton(onClick={deleting=debt}) { Text("حذف") }
                        }
                    }
                }
            }
        }
        Button(onClick={adding=true},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.Add,null); Text("ثبت بدهی یا طلب") }
    }
    if(adding) AlertDialog(onDismissRequest={adding=false},title={Text("بدهی یا طلب جدید")},text={
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name,{name=it},label={Text("نام شخص")},singleLine=true)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                FilterChip(selected=type==DebtType.I_OWE.name,onClick={type=DebtType.I_OWE.name},label={Text("من بدهکارم")})
                FilterChip(selected=type==DebtType.OWED_TO_ME.name,onClick={type=DebtType.OWED_TO_ME.name},label={Text("طلبکارم")})
            }
            OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},label={Text("مبلغ کل (تومان)")},singleLine=true)
            OutlinedTextField(due,{due=it.filter(Char::isDigit)},label={Text("سررسید (میلی‌ثانیه، اختیاری)")},singleLine=true)
            OutlinedTextField(note,{note=it},label={Text("توضیح")})
        }
    },confirmButton={TextButton(onClick={vm.save(name,type,amount.toLongOrNull()?:0L,due.toLongOrNull(),note);adding=false;name="";amount="";due="";note=""}){Text("ذخیره")}},dismissButton={TextButton(onClick={adding=false}){Text("انصراف")}})
    deleting?.let { debt -> AlertDialog(onDismissRequest={deleting=null},title={Text("حذف بدهی/طلب")},text={Text("تراکنش‌های مرتبط حذف نمی‌شوند و فقط ارتباط آن‌ها برداشته می‌شود. ادامه می‌دهید؟")},confirmButton={TextButton(onClick={vm.delete(debt);deleting=null}){Text("حذف")}},dismissButton={TextButton(onClick={deleting=null}){Text("انصراف")}}) }
}
