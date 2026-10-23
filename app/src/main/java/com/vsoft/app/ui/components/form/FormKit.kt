package com.vsoft.app.ui.components.form

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.JCal
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toPersianDigits

object FormStyle {
    val Blue1 = Color(0xFF2F6BFF)
    val Blue2 = Color(0xFF4F8DFF)
    val IconBlue = Color(0xFF6E9BFF)
    val gradient = Brush.horizontalGradient(listOf(Blue1, Blue2))
    val cardShape = RoundedCornerShape(20.dp)
    val fieldShape = RoundedCornerShape(14.dp)
}
object FormFormat {
    private fun two(n: Int) = n.toString().padStart(2, '0')
    fun date(millis: Long): String { val j = JCal.toJalali(millis); return "${j[0]}/${two(j[1])}/${two(j[2])}".toPersianDigits() }
    fun weekday(millis: Long): String = JCal.weekdayNames.getOrNull(JCal.weekdayIndex(millis)) ?: ""
    fun time(h: Int, m: Int) = "${two(h)}:${two(m)}".toPersianDigits()
    fun monthName(m: Int) = JCal.monthNames.getOrNull(m - 1) ?: ""
    fun groupDigits(raw: String): String = raw.reversed().chunked(3).joinToString(",").reversed().toPersianDigits()
}
@Composable
fun FormScaffold(title: String, subtitle: String, icon: ImageVector, onBack: () -> Unit, bottomBar: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = Vsoft.colors
    val bg = remember(c.isDark) { Brush.verticalGradient(listOf(c.bg, if (c.isDark) Color(0xFF0A1730) else c.bg)) }
    Column(Modifier.fillMaxSize().background(bg).statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(FormStyle.Blue1.copy(alpha=.18f)).border(1.dp, FormStyle.Blue1.copy(alpha=.5f), RoundedCornerShape(16.dp)), contentAlignment=Alignment.Center) { Icon(icon, null, tint=FormStyle.IconBlue) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, color=c.textPrimary, fontSize=20.sp, fontWeight=FontWeight.Bold); Text(subtitle, color=c.textSecondary, fontSize=13.sp) }
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.surface).border(1.dp,c.outline.copy(alpha=.7f),RoundedCornerShape(14.dp)).clickable(onClick=onBack), contentAlignment=Alignment.Center) { Icon(Icons.Default.ArrowForward,"بازگشت",tint=c.textPrimary) }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) { content(); Spacer(Modifier.height(4.dp)) }
        Box(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) { bottomBar() }
    }
}
@Composable
fun FormCard(icon: ImageVector, title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c=Vsoft.colors
    Column(modifier.fillMaxWidth().clip(FormStyle.cardShape).background(c.surface).border(1.dp,c.outline.copy(alpha=.55f),FormStyle.cardShape).padding(14.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) { Icon(icon,null,tint=FormStyle.IconBlue,modifier=Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)); Text(title,color=c.textPrimary,fontSize=16.sp,fontWeight=FontWeight.Bold) }
        content()
    }
}
@Composable
fun FormTextField(value:String,onValueChange:(String)->Unit,placeholder:String,modifier:Modifier=Modifier,singleLine:Boolean=true,minLines:Int=1,keyboardType:KeyboardType=KeyboardType.Text,trailing:(@Composable ()->Unit)?=null,minHeight:Dp=52.dp) {
    val c=Vsoft.colors
    Row(modifier.fillMaxWidth().heightIn(min=minHeight).clip(FormStyle.fieldShape).background(c.surfaceHigh).border(1.dp,c.outline.copy(alpha=.7f),FormStyle.fieldShape).padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=if(singleLine) Alignment.CenterVertically else Alignment.Top) {
        Box(Modifier.weight(1f)) {
            if(value.isEmpty()) Text(placeholder,color=c.textSecondary.copy(alpha=.8f),fontSize=15.sp)
            BasicTextField(value,onValueChange,singleLine=singleLine,minLines=minLines,keyboardOptions=KeyboardOptions(keyboardType=keyboardType),textStyle=TextStyle(color=c.textPrimary,fontSize=15.sp,textAlign=TextAlign.Start),cursorBrush=SolidColor(FormStyle.Blue2),modifier=Modifier.fillMaxWidth())
        }
        if(trailing!=null){Spacer(Modifier.width(10.dp));trailing()}
    }
}

private class GroupedAmountTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val grouped = raw.reversed().chunked(3).joinToString(",").reversed().toPersianDigits()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safe = offset.coerceIn(0, raw.length)
                var commas = 0
                for (i in 1 until safe) {
                    if ((raw.length - i) % 3 == 0) commas++
                }
                return (safe + commas).coerceIn(0, grouped.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, grouped.length)
                return grouped.take(safe).count { it != ',' }.coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

@Composable
fun FormAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = Vsoft.colors
    var field by remember { mutableStateOf(TextFieldValue(value, selection = TextRange(value.length))) }
    LaunchedEffect(value) {
        if (value != field.text) {
            field = TextFieldValue(value, selection = TextRange(value.length))
        }
    }
    Row(
        modifier.fillMaxWidth().heightIn(min = 52.dp).clip(FormStyle.fieldShape)
            .background(c.surfaceHigh).border(1.dp, c.outline.copy(alpha = .7f), FormStyle.fieldShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            if (field.text.isEmpty()) Text(placeholder, color = c.textSecondary.copy(alpha = .8f), fontSize = 15.sp)
            BasicTextField(
                value = field,
                onValueChange = { incoming ->
                    val filtered = incoming.text.toLatinDigits().filter { it.isDigit() }.take(15)
                    val selection = incoming.selection.end.coerceAtMost(filtered.length)
                    val updated = incoming.copy(text = filtered, selection = TextRange(selection))
                    field = updated
                    onValueChange(filtered)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = GroupedAmountTransformation(),
                textStyle = TextStyle(color = c.textPrimary, fontSize = 15.sp, textAlign = TextAlign.Start),
                cursorBrush = SolidColor(FormStyle.Blue2),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailing != null) { Spacer(Modifier.width(10.dp)); trailing() }
    }
}

@Composable
private fun TypePill(text:String,selected:Boolean,tint:Color,icon:ImageVector,onClick:()->Unit,modifier:Modifier) {
    val c=Vsoft.colors
    val a by animateFloatAsState(if(selected) 1f else 0f,tween(220),label="pill")
    val shape=RoundedCornerShape(16.dp)
    Box(modifier.height(48.dp).clip(shape).background(c.surfaceHigh,shape).background(FormStyle.gradient,shape,alpha=a).clickable(onClick=onClick),contentAlignment=Alignment.Center) {
        Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(26.dp).clip(CircleShape).background(tint),contentAlignment=Alignment.Center){Icon(icon,null,tint=Color.White,modifier=Modifier.size(16.dp))};Spacer(Modifier.width(10.dp));Text(text,color=if(selected) Color.White else c.textPrimary,fontSize=16.sp,fontWeight=FontWeight.Bold)}
    }
}
@Composable
fun FormTypeToggle(isIncome:Boolean,onChange:(Boolean)->Unit) {
    val c=Vsoft.colors;val shape=RoundedCornerShape(20.dp)
    Row(Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp,c.outline.copy(alpha=.6f),shape).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        TypePill("هزینه",!isIncome,ExpenseRed,Icons.Default.Remove,{onChange(false)},Modifier.weight(1f))
        TypePill("درآمد",isIncome,IncomeGreen,Icons.Default.Add,{onChange(true)},Modifier.weight(1f))
    }
}
@Composable
fun FormTile(label:String,icon:ImageVector,tint:Color,selected:Boolean,onClick:()->Unit) {
    val c=Vsoft.colors;val shape=RoundedCornerShape(18.dp)
    Column(Modifier.width(72.dp).clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).shadow(if(selected) 10.dp else 0.dp,shape,ambientColor=FormStyle.Blue1,spotColor=FormStyle.Blue1).clip(shape).background(tint.copy(alpha=.15f)).then(if(selected) Modifier.background(FormStyle.gradient) else Modifier).border(1.dp,if(selected) Color.Transparent else c.outline.copy(alpha=.4f),shape),contentAlignment=Alignment.Center) { Icon(icon,null,tint=if(selected) Color.White else tint,modifier=Modifier.size(28.dp)) }
        Spacer(Modifier.height(6.dp));Text(label,color=if(selected)c.textPrimary else c.textSecondary,fontSize=12.sp,maxLines=1,textAlign=TextAlign.Center)
    }
}
@Composable
fun FormPickerRow(icon:ImageVector,text:String,sub:String?=null,onClick:()->Unit,modifier:Modifier=Modifier) {
    val c=Vsoft.colors
    Row(modifier.fillMaxWidth().heightIn(min=60.dp).clip(FormStyle.fieldShape).background(c.surfaceHigh).border(1.dp,c.outline.copy(alpha=.7f),FormStyle.fieldShape).clickable(onClick=onClick).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)){Text(text,color=c.textPrimary,fontSize=15.sp,fontWeight=FontWeight.Medium);if(sub!=null)Text(sub,color=c.textSecondary,fontSize=12.sp)}
        Icon(Icons.Default.ChevronLeft,null,tint=c.textSecondary,modifier=Modifier.size(20.dp));Spacer(Modifier.width(6.dp));Icon(icon,null,tint=c.textSecondary,modifier=Modifier.size(24.dp))
    }
}
@Composable
fun FormPrimaryButton(text:String,enabled:Boolean,loading:Boolean,onClick:()->Unit) {
    val shape=RoundedCornerShape(28.dp)
    Box(Modifier.fillMaxWidth().height(56.dp).alpha(if(enabled)1f else .4f).clip(shape).background(FormStyle.gradient).clickable(enabled=enabled&&!loading,onClick=onClick),contentAlignment=Alignment.Center) {
        if(loading) CircularProgressIndicator(Modifier.size(24.dp),color=Color.White,strokeWidth=2.5.dp) else Row(verticalAlignment=Alignment.CenterVertically){Text(text,color=Color.White,fontSize=17.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));Icon(Icons.Default.Check,null,tint=Color.White)}
    }
}
data class FormAccountOption(val id:Long,val name:String,val balanceText:String)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormAccountSheet(accounts:List<FormAccountOption>,selectedId:Long?,onPick:(Long?)->Unit,onDismiss:()->Unit) {
    val c=Vsoft.colors
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=c.surface) {
        Column(Modifier.padding(16.dp).navigationBarsPadding(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("انتخاب حساب",color=c.textPrimary,fontSize=17.sp,fontWeight=FontWeight.Bold)
            FormPickerRow(Icons.Default.Remove,"بدون حساب",null,{onPick(null)},Modifier.then(if(selectedId==null)Modifier.border(1.5.dp,FormStyle.Blue2,FormStyle.fieldShape) else Modifier))
            accounts.forEach { a->FormPickerRow(Icons.Default.Check,a.name,"موجودی: ${a.balanceText} تومان",{onPick(a.id)},Modifier.then(if(a.id==selectedId)Modifier.border(1.5.dp,FormStyle.Blue2,FormStyle.fieldShape) else Modifier)) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDatePickerSheet(initialMillis:Long,onPick:(Long)->Unit,onDismiss:()->Unit) {
    val c=Vsoft.colors;val init=remember{JCal.toJalali(initialMillis)};val today=remember{JCal.toJalali(System.currentTimeMillis())}
    var vy by remember{mutableIntStateOf(init[0])};var vm by remember{mutableIntStateOf(init[1])};var sel by remember{mutableStateOf(Triple(init[0],init[1],init[2]))}
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=c.surface) {
        Column(Modifier.padding(horizontal=16.dp).padding(bottom=16.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={if(vm==1){vm=12;vy--}else vm--}){Icon(Icons.Default.ChevronRight,"ماه قبل",tint=c.textPrimary)};Text("${FormFormat.monthName(vm)} ${vy.toString().toPersianDigits()}",Modifier.weight(1f),color=c.textPrimary,fontSize=17.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);IconButton(onClick={if(vm==12){vm=1;vy++}else vm++}){Icon(Icons.Default.ChevronLeft,"ماه بعد",tint=c.textPrimary)}}
            Row(Modifier.fillMaxWidth()){for(i in 0..6)Text(JCal.dayHeaders.getOrNull(i)?: "",Modifier.weight(1f),color=c.textSecondary,fontSize=12.sp,textAlign=TextAlign.Center)}
            val first=JCal.weekdayIndex(JCal.fromJalali(vy,vm,1));val len=JCal.monthLength(vy,vm);val rows=(first+len+6)/7
            for(r in 0 until rows)Row(Modifier.fillMaxWidth()){for(col in 0..6){val d=r*7+col-first+1;Box(Modifier.weight(1f).height(44.dp),contentAlignment=Alignment.Center){if(d in 1..len){val isSel=sel==Triple(vy,vm,d);val isToday=today[0]==vy&&today[1]==vm&&today[2]==d;Box(Modifier.size(38.dp).clip(CircleShape).then(if(isSel)Modifier.background(FormStyle.gradient) else if(isToday)Modifier.border(1.dp,FormStyle.Blue2,CircleShape) else Modifier).clickable{sel=Triple(vy,vm,d)},contentAlignment=Alignment.Center){Text(JCal.fa(d),fontSize=14.sp,color=if(isSel)Color.White else if(col==6)ExpenseRed else c.textPrimary)}}}}}
            Spacer(Modifier.height(12.dp));FormPrimaryButton("تأیید",true,false){onPick(JCal.fromJalali(sel.first,sel.second,sel.third))}
        }
    }
}
@Composable
private fun TimeStepper(value:Int,onUp:()->Unit,onDown:()->Unit) {
    val c=Vsoft.colors
    Column(horizontalAlignment=Alignment.CenterHorizontally){IconButton(onClick=onUp){Icon(Icons.Default.KeyboardArrowUp,"افزایش",tint=c.textPrimary)};Text(value.toString().padStart(2,'0').toPersianDigits(),color=c.textPrimary,fontSize=40.sp,fontWeight=FontWeight.Bold);IconButton(onClick=onDown){Icon(Icons.Default.KeyboardArrowDown,"کاهش",tint=c.textPrimary)}}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTimePickerSheet(hour:Int,minute:Int,onPick:(Int,Int)->Unit,onDismiss:()->Unit) {
    val c=Vsoft.colors;var h by remember{mutableIntStateOf(hour.coerceIn(0,23))};var m by remember{mutableIntStateOf(minute.coerceIn(0,59))}
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=c.surface){Column(Modifier.padding(16.dp).navigationBarsPadding(),horizontalAlignment=Alignment.CenterHorizontally){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){TimeStepper(m,{m=(m+1)%60},{m=(m+59)%60});Text(":",color=c.textPrimary,fontSize=36.sp);TimeStepper(h,{h=(h+1)%24},{h=(h+23)%24})};Spacer(Modifier.height(12.dp));FormPrimaryButton("تأیید",true,false){onPick(h,m)}}}
}
