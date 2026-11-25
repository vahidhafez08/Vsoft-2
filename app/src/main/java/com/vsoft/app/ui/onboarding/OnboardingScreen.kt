@file:OptIn(ExperimentalFoundationApi::class)

package com.vsoft.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.R
import com.vsoft.app.ui.components.Appear
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.bounceClick
import com.vsoft.app.ui.screens.rememberReduceMotion
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.WarningOrange
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ───────────────────────────── Data ───────────────────────────── */

private class Step(
    val accent: Color,
    val accent2: Color,
    val title: String,
    val body: String,
    val points: List<Pair<ImageVector, String>>,
    val art: @Composable () -> Unit
)

private val ArtHeight = 250.dp

private val Steps: List<Step> = listOf(
    Step(
        BrandBlue, BrandPurple,
        "به Vesta خوش اومدی 👋",
        "دستیار مالی و کاری شخصی تو؛ حساب‌ها، هزینه‌ها، کارها و کارگاه‌هات همه یک‌جا. این راهنما حدود یک دقیقه طول می‌کشه و هر وقت خواستی می‌تونی ردش کنی.",
        listOf(
            Icons.Filled.Lock to "اطلاعاتت (تراکنش‌ها، کارها و اسناد) فقط روی همین گوشی ذخیره می‌شه",
            Icons.Filled.CalendarMonth to "رابط کاملاً فارسی با تقویم شمسی",
            Icons.Filled.Palette to "تم روشن، تیره یا هماهنگ با سیستم"
        )
    ) { ArtWelcome() },
    Step(
        BrandBlue, BrandPurple,
        "خانه؛ خلاصه‌ی وضعیتت",
        "همین که برنامه رو باز می‌کنی، موجودی کل، درآمد این ماه و هزینه‌ی امروزت رو می‌بینی.",
        listOf(
            Icons.Filled.TrendingUp to "نمودار درآمد و هزینه‌ی ۷ روز اخیر",
            Icons.Filled.Add to "میان‌بر ثبت هزینه، درآمد و انتقال وجه",
            Icons.Filled.Description to "دسترسی سریع به اسناد"
        )
    ) { ArtHome() },
    Step(
        IncomeGreen, BrandBlue,
        "ثبت تراکنش در چند ثانیه",
        "دکمه‌ی + رو بزن و انتخاب کن هزینه‌ست یا درآمد. مبلغ، حساب و دسته‌بندی رو وارد کن و ذخیره بزن.",
        listOf(
            Icons.Filled.Remove to "ثبت هزینه با دسته‌بندی",
            Icons.Filled.Add to "ثبت درآمد",
            Icons.Filled.SwapHoriz to "انتقال وجه بین حساب‌هات"
        )
    ) { ArtQuickAdd() },
    Step(
        BrandPurple, BrandBlue,
        "مالی؛ حساب‌ها و دسته‌بندی‌ها",
        "در بخش «مالی» درآمد و هزینه‌ی ماه، دسته‌بندی‌ها و موجودی هر حساب رو می‌بینی.",
        listOf(
            Icons.Filled.CreditCard to "چند حساب یا کارت، هرکدوم با موجودی جدا",
            Icons.Filled.Receipt to "ساخت فاکتور PDF یا تصویر از تراکنش",
            Icons.Filled.Savings to "پیگیری بدهی‌ها و سرمایه‌گذاری‌ها"
        )
    ) { ArtFinance() },
    Step(
        IncomeGreen, BrandBlue,
        "کارها و پروژه‌ها",
        "کارهات رو توی پروژه‌ها دسته‌بندی کن، اولویت و موعد بذار و با زیروظیفه‌ها ریزشون کن.",
        listOf(
            Icons.Filled.Flag to "اولویت و موعد برای هر کار",
            Icons.Filled.CalendarMonth to "تقویم شمسی؛ از بخش «کارها» بازش کن",
            Icons.Filled.Notifications to "یادآوری روزانه؛ از «بیشتر» فعالش کن"
        )
    ) { ArtTasks() },
    Step(
        WarningOrange, ExpenseRed,
        "کارگاه‌ها و محل‌های کار",
        "برای پروژه‌های اجرایی یک محل کار تعریف کن و طبقه‌ها، آیتم‌ها، گزارش‌ها و پرداخت‌ها رو اونجا ثبت کن. از صفحه‌ی «کارها» بازش می‌کنی.",
        listOf(
            Icons.Filled.Layers to "طبقه‌ها و آیتم‌های اجرایی",
            Icons.Filled.Edit to "گزارش کار روزانه",
            Icons.Filled.Payments to "ثبت پرداخت‌ها و پیشرفت کار"
        )
    ) { ArtWorkshop() },
    Step(
        BrandBlue, BrandPurple,
        "گزارش‌ها؛ پولت کجا می‌ره؟",
        "خلاصه‌ی درآمد، هزینه و تراز رو ببین و با دوره‌ی قبل مقایسه کن.",
        listOf(
            Icons.Filled.BarChart to "بازه‌ی هفته، ماه، سه‌ماهه و سال",
            Icons.Filled.PieChart to "سهم هر دسته‌بندی از هزینه‌ها",
            Icons.Filled.TrendingUp to "نمودار روند و میانگین هزینه‌ی روزانه"
        )
    ) { ArtReports() },
    Step(
        BrandPurple, BrandBlue,
        "اسناد؛ فایل‌های مهمت",
        "مدارک و فایل‌های مهم رو اضافه و دسته‌بندی کن؛ هر وقت لازم شد جستجو، باز یا اشتراک‌گذاریشون کن.",
        listOf(
            Icons.Filled.Search to "جستجوی سریع بین اسناد",
            Icons.Filled.Folder to "دسته‌بندی برای نظم بیشتر",
            Icons.Filled.Share to "اشتراک‌گذاری مستقیم فایل"
        )
    ) { ArtDocs() },
    Step(
        BrandBlue, IncomeGreen,
        "امنیت و پشتیبان‌گیری",
        "از بخش «بیشتر» می‌تونی برنامه رو قفل کنی، از اطلاعاتت بکاپ بگیری و ظاهر برنامه رو عوض کنی.",
        listOf(
            Icons.Filled.Lock to "قفل با رمز ۴ رقمی و مخفی‌کردن پیش‌نمایش برنامه",
            Icons.Filled.CloudUpload to "بکاپ، بازیابی و خروجی CSV برای اکسل",
            Icons.Filled.Info to "توجه: خودِ فایل‌های اسناد داخل بکاپ نیستن"
        )
    ) { ArtSecurity() },
    Step(
        IncomeGreen, BrandBlue,
        "همه‌چیز آماده‌ست 🎉",
        "اگه دوست داری اسمت رو بنویس تا برنامه باهات صمیمی‌تر باشه (اختیاریه). این آموزش رو هر وقت خواستی از «بیشتر ← آموزش برنامه» دوباره می‌بینی.",
        emptyList()
    ) { ArtFinish() }
)

/* ───────────────────────────── Screen ───────────────────────────── */

/** Step-by-step intro. [onFinish] gets the typed display name (or null). Also called when the user skips. */
@Composable
fun OnboardingScreen(onFinish: (String?) -> Unit) {
    val c = Vsoft.colors
    val pager = rememberPagerState { Steps.size }
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    val page = pager.currentPage
    val isLast = page == Steps.lastIndex
    val accent by animateColorAsState(Steps[page].accent, tween(500), label = "accent")
    val accent2 by animateColorAsState(Steps[page].accent2, tween(500), label = "accent2")

    fun finish() = onFinish(name.trim().ifEmpty { null })
    fun goTo(i: Int) { scope.launch { pager.animateScrollToPage(i) } }

    BackHandler(enabled = page > 0) { goTo(page - 1) }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = if (c.isDark) 0.24f else 0.13f), Color.Transparent),
                        endY = 1100f
                    )
                )
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // Top bar: step counter + skip
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${page + 1} از ${Steps.size}".toFa(),
                    color = c.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                if (!isLast) {
                    TextButton(onClick = { finish() }) {
                        Text("رد کردن", color = c.textSecondary, fontSize = 14.sp)
                    }
                }
            }

            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                key = { it }
            ) { i ->
                StepPage(
                    step = Steps[i],
                    active = pager.currentPage == i,
                    isLast = i == Steps.lastIndex,
                    name = name,
                    onName = { name = it.take(24) },
                    onDone = { finish() }
                )
            }

            // Bottom: dots + buttons
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Steps.indices.forEach { i ->
                        val w by animateDpAsState(if (i == page) 26.dp else 8.dp, tween(300), label = "dot")
                        Box(
                            Modifier.height(8.dp).width(w).clip(CircleShape)
                                .background(if (i == page) accent else c.outline)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (page > 0) {
                        val shape = RoundedCornerShape(18.dp)
                        Box(
                            Modifier.size(54.dp)
                                .bounceClick { goTo(page - 1) }
                                .clip(shape).background(c.surface).border(1.dp, c.outline, shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "مرحله قبل", tint = c.textPrimary)
                        }
                    }
                    Box(
                        Modifier.weight(1f).height(54.dp)
                            .bounceClick { if (isLast) finish() else goTo(page + 1) }
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.horizontalGradient(listOf(accent, accent2))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                if (isLast) "شروع کنید" else "مرحله بعد",
                                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                            Icon(
                                if (isLast) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                null, tint = Color.White, modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepPage(
    step: Step,
    active: Boolean,
    isLast: Boolean,
    name: String,
    onName: (String) -> Unit,
    onDone: () -> Unit
) {
    val c = Vsoft.colors
    val enter by animateFloatAsState(if (active) 1f else 0f, tween(450, easing = FastOutSlowInEasing), label = "enter")
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().graphicsLayer {
                alpha = 0.35f + 0.65f * enter
                scaleX = 0.94f + 0.06f * enter
                scaleY = 0.94f + 0.06f * enter
            }
        ) { step.art() }
        Spacer(Modifier.height(18.dp))
        Column(
            Modifier.fillMaxWidth().graphicsLayer { alpha = enter; translationY = (1f - enter) * 40f },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(step.title, color = c.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
            Text(step.body, color = c.textSecondary, fontSize = 14.sp, lineHeight = 24.sp)
        }
        if (isLast) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("نام نمایشی (اختیاری)") },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = step.accent,
                    unfocusedBorderColor = c.outline,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary,
                    focusedLabelColor = step.accent,
                    unfocusedLabelColor = c.textSecondary,
                    cursorColor = step.accent
                )
            )
        }
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            step.points.forEachIndexed { i, (icon, text) ->
                Appear(index = i + 2) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(icon, step.accent, 34.dp)
                        Text(text, color = c.textPrimary, fontSize = 13.5.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

/* ───────────────────────────── Small building blocks ───────────────────────────── */

private fun String.toFa(): String =
    map { if (it in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[it - '0'] else it }.joinToString("")

@Composable
private fun rememberLoop(millis: Int): Float {
    val reduce = rememberReduceMotion()
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "loop")
    val v by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(millis, easing = LinearEasing)),
        label = "v"
    )
    return if (reduce) 0.7f else v
}

@Composable
private fun rememberGrow(durationMs: Int = 900, delayMs: Int = 150): Float {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val v by animateFloatAsState(
        if (go) 1f else 0f,
        tween(durationMillis = durationMs, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "grow"
    )
    return v
}

@Composable
private fun MockCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = Vsoft.colors
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.clip(shape).background(c.surface).border(1.dp, c.outline, shape).padding(12.dp),
        content = content
    )
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size / 3.2f)).background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size / 2)) }
}

@Composable
private fun Pill(icon: ImageVector?, text: String, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = tint, modifier = Modifier.size(15.dp))
        Text(text, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Vsoft.colors.surfaceHigh)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
    }
}

/* ───────────────────────────── Illustrations ───────────────────────────── */

@Composable
private fun ArtWelcome() {
    val loop = rememberLoop(2800)
    val bob = sin(loop * 2 * PI).toFloat()
    Box(Modifier.fillMaxWidth().height(ArtHeight), contentAlignment = Alignment.Center) {
        listOf(0f, 0.5f).forEach { off ->
            val p = (loop + off) % 1f
            Box(
                Modifier.size(120.dp + 130.dp * p)
                    .graphicsLayer { alpha = (1f - p) * 0.35f }
                    .border(2.dp, BrandBlue, CircleShape)
            )
        }
        Image(
            painterResource(R.drawable.vesta_logo), "Vesta",
            Modifier.size(108.dp).clip(RoundedCornerShape(30.dp))
        )
        Pill(Icons.Filled.AccountBalanceWallet, "مالی", BrandBlue,
            Modifier.align(Alignment.TopStart).offset(x = 6.dp, y = 28.dp + (bob * 6).dp))
        Pill(Icons.Filled.TaskAlt, "کارها", IncomeGreen,
            Modifier.align(Alignment.CenterEnd).offset(x = (-2).dp, y = (-bob * 6).dp))
        Pill(Icons.Filled.BarChart, "گزارش‌ها", BrandPurple,
            Modifier.align(Alignment.BottomStart).offset(x = 18.dp, y = (-26).dp + (bob * 5).dp))
    }
}

@Composable
private fun ArtHome() {
    val grow = rememberGrow()
    val c = Vsoft.colors
    val inc = listOf(0.45f, 0.7f, 0.35f, 0.8f, 0.55f, 0.9f, 0.6f)
    val exp = listOf(0.3f, 0.4f, 0.55f, 0.35f, 0.6f, 0.45f, 0.3f)
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple))).padding(12.dp)
            ) {
                Text("موجودی کل", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text("۱۲٬۴۵۰٬۰۰۰", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("تومان", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            MockCard(Modifier.weight(1f)) {
                Text("درآمد (این ماه)", color = c.textSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text("۸٬۲۰۰٬۰۰۰", color = IncomeGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("▲ ۱۲٪", color = IncomeGreen, fontSize = 11.sp)
            }
        }
        MockCard(Modifier.fillMaxWidth().weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("نمای کلی مالی (۷ روز اخیر)", color = c.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Pill(null, "نمونه", c.textSecondary)
            }
            Row(
                Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                inc.indices.forEach { i ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                        Box(Modifier.width(8.dp).fillMaxHeight(inc[i] * grow).clip(RoundedCornerShape(4.dp)).background(IncomeGreen))
                        Box(Modifier.width(8.dp).fillMaxHeight(exp[i] * grow).clip(RoundedCornerShape(4.dp)).background(ExpenseRed))
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtQuickAdd() {
    val c = Vsoft.colors
    val loop = rememberLoop(3800)
    val open = loop in 0.15f..0.85f
    val p by animateFloatAsState(if (open) 1f else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "open")
    Box(Modifier.fillMaxWidth().height(ArtHeight)) {
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(14.dp)).background(c.surface).border(1.dp, c.outline, RoundedCornerShape(14.dp)))
            }
        }
        Column(
            Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("انتقال وجه", Icons.Filled.SwapHoriz, BrandBlue),
                Triple("ثبت درآمد", Icons.Filled.Add, IncomeGreen),
                Triple("ثبت هزینه", Icons.Filled.Remove, ExpenseRed)
            ).forEach { (label, icon, tint) ->
                Row(
                    Modifier.graphicsLayer { alpha = p.coerceIn(0f, 1f); translationY = (1f - p) * 60f }
                        .clip(RoundedCornerShape(50)).background(c.surface)
                        .border(1.dp, c.outline, RoundedCornerShape(50))
                        .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(label, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Box(Modifier.size(32.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(Brush.linearGradient(listOf(BrandBlue, BrandPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp).rotate(45f * p))
            }
        }
    }
}

private class FRow(val name: String, val value: String, val frac: Float, val color: Color)

@Composable
private fun ArtFinance() {
    val loop = rememberLoop(6400)
    val idx = (loop * 4).toInt().coerceIn(0, 3)
    val data = listOf(
        listOf(FRow("درآمد", "۸٬۲۰۰٬۰۰۰", 1f, IncomeGreen), FRow("هزینه", "۵٬۱۰۰٬۰۰۰", 0.62f, ExpenseRed), FRow("تراز", "۳٬۱۰۰٬۰۰۰", 0.38f, BrandBlue)),
        listOf(FRow("حقوق", "۶٬۵۰۰٬۰۰۰", 0.8f, IncomeGreen), FRow("پروژه", "۱٬۷۰۰٬۰۰۰", 0.21f, IncomeGreen), FRow("سایر", "۰", 0.02f, IncomeGreen)),
        listOf(FRow("خوراک", "۲٬۱۰۰٬۰۰۰", 0.41f, ExpenseRed), FRow("حمل‌ونقل", "۱٬۲۰۰٬۰۰۰", 0.24f, WarningOrange), FRow("قبوض", "۸۰۰٬۰۰۰", 0.16f, BrandPurple)),
        listOf(FRow("حساب جاری", "۷٬۰۰۰٬۰۰۰", 0.56f, BrandBlue), FRow("پس‌انداز", "۵٬۴۵۰٬۰۰۰", 0.44f, BrandPurple), FRow("نقد", "۰", 0.02f, IncomeGreen))
    )
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SegmentedTabs(listOf("کلیات", "درآمد", "هزینه", "حساب‌ها"), idx, onSelect = {})
        Crossfade(idx, label = "fin") { t ->
            MockCard(Modifier.fillMaxWidth()) {
                data[t].forEachIndexed { i, r ->
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.name, color = Vsoft.colors.textPrimary, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Text(r.value, color = r.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(5.dp))
                    Bar(r.frac, r.color)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(Icons.Filled.Savings, "بدهی‌ها", WarningOrange)
            Pill(Icons.Filled.TrendingUp, "سرمایه‌گذاری", IncomeGreen)
            Pill(Icons.Filled.Receipt, "فاکتور", BrandPurple)
        }
    }
}

@Composable
private fun ArtTasks() {
    val c = Vsoft.colors
    val loop = rememberLoop(4200)
    val done = loop > 0.5f
    val progress by animateFloatAsState(if (done) 0.67f else 0.33f, tween(500), label = "p")
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MockCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("پروژه‌ی وب‌سایت", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(if (done) "۶۷٪" else "۳۳٪", color = IncomeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Bar(progress, IncomeGreen)
        }
        TaskRow("طراحی صفحه‌ی اصلی", true, ExpenseRed, "دیروز")
        TaskRow("ارسال فاکتور مشتری", done, WarningOrange, "امروز")
        TaskRow("جلسه با تیم", false, BrandBlue, "۲۰ آذر")
    }
}

@Composable
private fun TaskRow(title: String, done: Boolean, prio: Color, due: String) {
    val c = Vsoft.colors
    val fill by animateColorAsState(if (done) IncomeGreen else Color.Transparent, tween(250), label = "fill")
    MockCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(fill)
                    .border(1.5.dp, if (done) IncomeGreen else c.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) { if (done) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
            Text(
                title,
                color = if (done) c.textSecondary else c.textPrimary,
                fontSize = 13.sp,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            Pill(Icons.Filled.CalendarMonth, due, prio)
        }
    }
}

@Composable
private fun ArtWorkshop() {
    val c = Vsoft.colors
    val grow = rememberGrow(1100)
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MockCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconBadge(Icons.Filled.Apartment, WarningOrange, 38.dp)
                Column(Modifier.weight(1f)) {
                    Text("ساختمان نیاوران", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("پیشرفت کلی ۶۵٪", color = c.textSecondary, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Bar(0.65f * grow, WarningOrange)
        }
        MockCard(Modifier.fillMaxWidth()) {
            listOf(Triple("طبقه‌ی ۱", 0.9f, IncomeGreen), Triple("طبقه‌ی ۲", 0.55f, WarningOrange), Triple("طبقه‌ی ۳", 0.2f, ExpenseRed))
                .forEachIndexed { i, (n, f, col) ->
                    if (i > 0) Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(col))
                        Text(n, color = c.textPrimary, fontSize = 12.sp, modifier = Modifier.width(56.dp))
                        Bar(f * grow, col, Modifier.weight(1f))
                    }
                }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(Icons.Filled.Edit, "گزارش روزانه", BrandBlue)
            Pill(Icons.Filled.Payments, "پرداخت‌ها", IncomeGreen)
            Pill(Icons.Filled.Build, "آیتم‌ها", BrandPurple)
        }
    }
}

private val ReportSeries: List<Pair<List<Float>, List<Float>>> = listOf(
    listOf(0.3f, 0.5f, 0.4f, 0.7f, 0.6f, 0.8f, 0.9f) to listOf(0.5f, 0.4f, 0.6f, 0.3f, 0.5f, 0.4f, 0.35f),
    listOf(0.4f, 0.35f, 0.6f, 0.55f, 0.75f, 0.7f, 0.85f) to listOf(0.6f, 0.5f, 0.45f, 0.5f, 0.4f, 0.45f, 0.3f),
    listOf(0.2f, 0.4f, 0.5f, 0.45f, 0.7f, 0.85f, 0.8f) to listOf(0.45f, 0.5f, 0.35f, 0.4f, 0.5f, 0.3f, 0.35f),
    listOf(0.3f, 0.25f, 0.5f, 0.65f, 0.6f, 0.9f, 0.95f) to listOf(0.4f, 0.45f, 0.5f, 0.35f, 0.4f, 0.4f, 0.3f)
)

private fun DrawScope.drawSeries(vals: List<Float>, color: Color, progress: Float) {
    val path = Path()
    vals.forEachIndexed { i, v ->
        val x = size.width * i / (vals.size - 1)
        val y = size.height * (1f - v)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    clipRect(right = size.width * progress) {
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ArtReports() {
    val c = Vsoft.colors
    val loop = rememberLoop(7200)
    val tab = (loop * 4).toInt().coerceIn(0, 3)
    val seg = (loop * 4f - tab).coerceIn(0f, 1f)
    val prog = FastOutSlowInEasing.transform((seg * 1.5f).coerceAtMost(1f))
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SegmentedTabs(listOf("هفته", "ماه", "۳ ماه", "سال"), tab, onSelect = {})
        MockCard(Modifier.fillMaxWidth().weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column { Text("درآمد", color = c.textSecondary, fontSize = 11.sp); Text("۸٬۲۰۰٬۰۰۰", color = IncomeGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                Column { Text("هزینه", color = c.textSecondary, fontSize = 11.sp); Text("۵٬۱۰۰٬۰۰۰", color = ExpenseRed, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                Column { Text("تراز", color = c.textSecondary, fontSize = 11.sp); Text("۳٬۱۰۰٬۰۰۰", color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
            Canvas(Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp, bottom = 4.dp)) {
                val (a, b) = ReportSeries[tab]
                drawSeries(b, ExpenseRed, prog)
                drawSeries(a, IncomeGreen, prog)
            }
        }
    }
}

@Composable
private fun ArtDocs() {
    val c = Vsoft.colors
    Column(Modifier.fillMaxWidth().height(ArtHeight), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surface)
                .border(1.dp, c.outline, RoundedCornerShape(14.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Search, null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
            Text("جستجو در اسناد...", color = c.textSecondary, fontSize = 12.sp)
        }
        listOf(
            Triple(Icons.Filled.PictureAsPdf to ExpenseRed, "قرارداد اجاره", "مالی"),
            Triple(Icons.Filled.Photo to BrandBlue, "کارت ملی", "شخصی"),
            Triple(Icons.Filled.Description to BrandPurple, "فاکتور خرید", "کاری")
        ).forEachIndexed { i, (iconColor, title, cat) ->
            Appear(index = i) {
                MockCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconBadge(iconColor.first, iconColor.second, 38.dp)
                        Text(title, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Pill(null, cat, iconColor.second)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtSecurity() {
    val loop = rememberLoop(4800)
    val filled = (loop * 6).toInt().coerceIn(0, 5).coerceAtMost(4)
    val unlocked = loop * 6 >= 4.5f
    val circle by animateColorAsState(if (unlocked) IncomeGreen else BrandBlue, tween(300), label = "lock")
    Column(
        Modifier.fillMaxWidth().height(ArtHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(circle), contentAlignment = Alignment.Center) {
            Icon(if (unlocked) Icons.Filled.Check else Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier.size(14.dp).clip(CircleShape)
                        .background(if (i < filled) circle else Vsoft.colors.outline)
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(Icons.Filled.CloudUpload, "بکاپ", BrandBlue)
            Pill(Icons.Filled.FileDownload, "CSV", BrandPurple)
            Pill(Icons.Filled.Palette, "تم", WarningOrange)
        }
    }
}

@Composable
private fun ArtFinish() {
    val loop = rememberLoop(5000)
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val scale by animateFloatAsState(
        if (go) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "pop"
    )
    val dots = listOf(BrandBlue, BrandPurple, IncomeGreen, WarningOrange, ExpenseRed, BrandBlue, BrandPurple, IncomeGreen)
    Box(Modifier.fillMaxWidth().height(ArtHeight), contentAlignment = Alignment.Center) {
        dots.forEachIndexed { i, col ->
            val ang = (loop + i / dots.size.toFloat()) * 2 * PI
            Box(
                Modifier
                    .offset {
                        IntOffset(
                            (cos(ang) * 92.dp.toPx() * scale).toInt(),
                            (sin(ang) * 92.dp.toPx() * scale).toInt()
                        )
                    }
                    .size(if (i % 2 == 0) 10.dp else 7.dp)
                    .clip(CircleShape).background(col)
            )
        }
        Box(
            Modifier.size(104.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape).background(Brush.linearGradient(listOf(IncomeGreen, BrandBlue))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(56.dp)) }
    }
}