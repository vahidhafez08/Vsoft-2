package com.vsoft.app.ui.screens.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianText

@Composable
fun JobsScreen(onBack: () -> Unit, onOpenSite: (Long) -> Unit, viewModel: JobsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Vsoft.colors.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                ScreenHeader(title = "شغل و کارگاه‌ها", subtitle = "گزارش روزانه و پیشرفت کار هر ساختمان", onBack = onBack) {}
            }
            if (state.loaded && state.sites.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.Construction,
                        title = "هنوز کارگاهی ثبت نشده",
                        subtitle = "با دکمه + اولین کارگاه (ساختمان) را بساز"
                    )
                }
            }
            items(state.sites, key = { "site" + it.site.id }) { card ->
                SiteCard(card) { onOpenSite(card.site.id) }
            }
        }

        FloatingActionButton(
            onClick = { adding = true },
            containerColor = BrandBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "کارگاه جدید") }
    }

    if (adding) {
        JobSiteSheet(
            initial = null,
            onDismiss = { adding = false },
            onSave = { viewModel.saveSite(it); adding = false }
        )
    }
}

@Composable
private fun SiteCard(card: SiteCardUi, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val accent = if (card.progress >= 100) IncomeGreen else BrandBlue
    val site = card.site
    Column(
        Modifier
            .fillMaxWidth()
            .bounceClick(onClick)
            .clip(shape)
            .background(Vsoft.colors.surface)
            .border(1.dp, Vsoft.colors.outline, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            site.name, color = Vsoft.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        val sub = listOf(site.client, site.address).filter { it.isNotBlank() }.joinToString(" - ")
        if (sub.isNotEmpty()) {
            Text(sub, color = Vsoft.colors.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        AnimatedProgressBar(card.progress / 100f, accent)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                card.progress.toPersianText() + "٪", color = accent, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
            )
            Text(
                card.itemsDone.toPersianText() + " از " + card.itemsTotal.toPersianText() + " کار تمام شده",
                color = Vsoft.colors.textSecondary, fontSize = 11.sp
            )
        }
        if (site.contractAmount > 0L) {
            Text(
                "دریافتی: " + card.received.toMoney() + " از " + site.contractAmount.toMoney() + " تومان",
                color = Vsoft.colors.textSecondary, fontSize = 11.sp
            )
        }
    }
}
