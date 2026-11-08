package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.data.model.InvestmentTypeEntity
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(onBack:()->Unit,vm:InvestmentsViewModel=hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var formOpen by remember { mutableStateOf(false) }
    var typeOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InvestmentEntity?>(null) }
    var typeId by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("star") }
    var customColor by remember { mutableStateOf("#607D8B") }
    var priceTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var priceRaw by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf<InvestmentTypeEntity?>(null) }
    var groupPrice by remember { mutableStateOf("") }
    var sellTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var sellPrice by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    val summary=state.summary
    val green=Color(0xFF2E9E62); val red=Color(0xFFE05252)
    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text("سرمایه‌گذاری",color=Vsoft.colors.textPrimary,fontSize=22.sp)
            IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,null)}
        }
        Card(colors=CardDefaults.cardColors(containerColor=Vsoft.colors.surface),modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("مجموع سرمایه‌گذاری‌شده: ${summary.totalInvested.setScale(0,RoundingMode.HALF_UP).toPlainString()} تومان",color=Vsoft.colors.textPrimary)
                Text("ارزش فعلی: ${summary.totalCurrentValue.setScale(0,RoundingMode.HALF_UP).toPlainString()} تومان",color=Vsoft.colors.textPrimary)
                val pl=summary.totalProfitLoss
                Text("سود/زیان: ${if(pl.signum()>0)"+" else ""}${pl.toPlainString()} تومان  ·  ${summary.totalPercent?.toPlainString() ?: "—"}٪",color=if(pl.signum()>=0)green else red)
                if(summary.unpricedCount>0) Text("قیمت ${summary.unpricedCount} مورد وارد نشده و در جمع سود لحاظ نشده است",color=Vsoft.colors.textSecondary,fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=12.dp)) {
            state.types.forEach { type ->
                val group=state.items.filter { it.typeId==type.id }
                if(group.isNotEmpty()) {
                    item(key="header${type.id}") {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                            Text(type.name,color=Vsoft.colors.textPrimary,fontSize=17.sp)
                            TextButton(onClick={groupType=type;groupPrice=""}){Text("قیمت گروهی")}
                        }
                    }
                    items(group,key={it.id}) { item ->
                        val result=InvestmentCalculator.compute(item)
                        Card(colors=CardDefaults.cardColors(containerColor=Vsoft.colors.surface)) {
                            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                                Text(item.title.ifBlank{type.name},color=Vsoft.colors.textPrimary,fontSize=16.sp)
                                Text("مقدار: ${item.quantity} ${type.unitLabel} · قیمت خرید: ${item.buyPrice}",color=Vsoft.colors.textSecondary,fontSize=12.sp)
                                Text("سرمایه: ${item.investedAmount.toMoney()} تومان",color=Vsoft.colors.textSecondary,fontSize=12.sp)
                                Text("ارزش فعلی: ${result.currentValue?.setScale(0,RoundingMode.HALF_UP)?.toPlainString() ?: "قیمت فعلی را وارد کنید"}",color=Vsoft.colors.textPrimary)
                                Text(if(result.profitLoss==null)"سود و زیان: —" else "سود/زیان: ${result.profitLoss.toPlainString()} (${result.profitLossPercent?.toPlainString() ?: "—"}٪)",color=if((result.profitLoss?.signum()?:0)>=0)green else red)
                                Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick={editing=item;typeId=item.typeId;title=item.title;amount=item.investedAmount.toString();buyPrice=item.buyPrice.toString();quantity=item.quantity.toString();note=item.note;formOpen=true}){Text("ویرایش")}
                                    TextButton(onClick={priceTarget=item;priceRaw=""}){Text("بروزرسانی قیمت")}
                                    if(item.status=="ACTIVE") TextButton(onClick={sellTarget=item;sellPrice=""}){Text("فروخته شد")}
                                    TextButton(onClick={deleteTarget=item}){Text("حذف")}
                                }
                            }
                        }
                    }
                }
            }
            item {
                state.types.filterNot { it.isDefault }.forEach { type ->
                    TextButton(onClick={vm.deleteType(type)}){Text("حذف نوع «${type.name}»")}
                }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick={typeOpen=true},modifier=Modifier.weight(1f)){Text("+ نوع جدید")}
            Button(onClick={editing=null;typeId=state.types.firstOrNull()?.id?:0L;title="";amount="";buyPrice="";quantity="";note="";formOpen=true},modifier=Modifier.weight(1f)){Icon(Icons.Default.Add,null);Text("ثبت سرمایه")}
        }
    }
    if(formOpen) ModalBottomSheet(onDismissRequest={formOpen=false},containerColor=Vsoft.colors.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(if(editing==null)"ثبت سرمایه" else "ویرایش سرمایه",color=Vsoft.colors.textPrimary,fontSize=20.sp)
            Text("نوع سرمایه",color=Vsoft.colors.textSecondary)
            Row { state.types.forEach { FormChip(text = it.name, selected = typeId == it.id, onClick = { typeId = it.id }) } }
            FormCard(Icons.Default.TrendingUp,"مشخصات سرمایه") {
                FormTextField(title,{title=it},"عنوان (اختیاری)")
                FormAmountField(value=amount,onValueChange={amount=it; val q=it.toLongOrNull();val p=buyPrice.toDoubleOrNull();if(q!=null&&p!=null&&p>0)quantity=com.vsoft.app.domain.InvestmentCalculator.quantityFrom(q,p)?.toString().orEmpty()},placeholder="مبلغ سرمایه‌گذاری")
                FormNumberField(buyPrice,{buyPrice=it;val a=amount.toLongOrNull();val p=it.toDoubleOrNull();if(a!=null&&p!=null&&p>0)quantity=InvestmentCalculator.quantityFrom(a,p)?.toString().orEmpty()},"قیمت خرید","تومان")
                FormNumberField(quantity,{quantity=it;val q=it.toDoubleOrNull();val p=buyPrice.toDoubleOrNull();if(q!=null&&p!=null&&p>0)amount=InvestmentCalculator.investedFrom(q,p)?.toString().orEmpty()},"مقدار",state.types.firstOrNull{it.id==typeId}?.unitLabel)
                FormTextField(note,{note=it},"یادداشت")
            }
            FormPrimaryButton("ذخیره سرمایه",typeId>0 && (amount.toLongOrNull()?:0)>0 && (buyPrice.toDoubleOrNull()?:0.0)>0 && (quantity.toDoubleOrNull()?:0.0)>0,false) {
                vm.save(InvestmentEntity(id=editing?.id?:0L,typeId=typeId,title=title,investedAmount=amount.toLong(),buyPrice=buyPrice.toDouble(),quantity=quantity.toDouble(),buyDateMillis=editing?.buyDateMillis?:System.currentTimeMillis(),note=note))
                formOpen=false
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    if(typeOpen) AlertDialog(onDismissRequest={typeOpen=false},title={Text("نوع سرمایه جدید")},text={
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(customName,{customName=it},label={Text("نام نوع")})
            OutlinedTextField(customUnit,{customUnit=it},label={Text("واحد")})
            OutlinedTextField(customIcon,{customIcon=it},label={Text("کلید آیکن")})
            OutlinedTextField(customColor,{customColor=it},label={Text("رنگ HEX")})
        }
    },confirmButton={TextButton(onClick={vm.saveType(customName,customUnit,customIcon,customColor);typeOpen=false;customName="";customUnit=""}){Text("ذخیره")}},dismissButton={TextButton(onClick={typeOpen=false}){Text("انصراف")}})
    priceTarget?.let { item -> AlertDialog(onDismissRequest={priceTarget=null},title={Text("بروزرسانی قیمت")},text={OutlinedTextField(priceRaw,{priceRaw=it},label={Text("قیمت هر واحد")})},confirmButton={TextButton(onClick={priceRaw.toDoubleOrNull()?.let{vm.updatePrice(item,it)};priceTarget=null}){Text("ثبت")}},dismissButton={TextButton(onClick={priceTarget=null}){Text("انصراف")}}) }
    groupType?.let { type -> AlertDialog(onDismissRequest={groupType=null},title={Text("قیمت گروهی ${type.name}")},text={OutlinedTextField(groupPrice,{groupPrice=it},label={Text("قیمت جدید هر واحد")})},confirmButton={TextButton(onClick={groupPrice.toDoubleOrNull()?.let{vm.updateGroup(type.id,it)};groupType=null}){Text("اعمال")}},dismissButton={TextButton(onClick={groupType=null}){Text("انصراف")}}) }
    sellTarget?.let { item -> AlertDialog(onDismissRequest={sellTarget=null},title={Text("ثبت فروش")},text={OutlinedTextField(sellPrice,{sellPrice=it},label={Text("قیمت فروش هر واحد")})},confirmButton={TextButton(onClick={sellPrice.toDoubleOrNull()?.let{vm.sell(item,it)};sellTarget=null}){Text("ثبت فروش")}},dismissButton={TextButton(onClick={sellTarget=null}){Text("انصراف")}}) }
    deleteTarget?.let { item -> AlertDialog(onDismissRequest={deleteTarget=null},title={Text("حذف سرمایه")},text={Text("این سرمایه حذف شود؟")},confirmButton={TextButton(onClick={vm.delete(item);deleteTarget=null}){Text("حذف")}},dismissButton={TextButton(onClick={deleteTarget=null}){Text("انصراف")}}) }
}
