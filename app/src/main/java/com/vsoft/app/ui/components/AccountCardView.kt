package com.vsoft.app.ui.components

import com.vsoft.app.ui.theme.Dimens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.util.toPersianDigits

val AccountColorKeys = listOf("blue", "dark", "green", "purple", "red", "orange", "teal")

private val accountColorPairs: Map<String, Pair<Color, Color>> = mapOf(
    "blue" to Pair(Color(0xFF1E3A8A), Color(0xFF3B6CF6)),
    "dark" to Pair(Color(0xFF111827), Color(0xFF374151)),
    "green" to Pair(Color(0xFF14532D), Color(0xFF16A34A)),
    "purple" to Pair(Color(0xFF5B21B6), Color(0xFF8B5CF6)),
    "red" to Pair(Color(0xFF991B1B), Color(0xFFEF4444)),
    "orange" to Pair(Color(0xFF9A3412), Color(0xFFF59E0B)),
    "teal" to Pair(Color(0xFF115E59), Color(0xFF14B8A6))
)

private fun pairOf(key: String): Pair<Color, Color> =
    accountColorPairs[key] ?: accountColorPairs.getValue("blue")

fun accountBrush(key: String): Brush {
    val p = pairOf(key)
    return Brush.linearGradient(listOf(p.first, p.second))
}

fun accountSwatch(key: String): Color = pairOf(key).second

@Composable
fun AccountCardView(
    name: String,
    last4: String,
    balance: Long,
    colorKey: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.cornerCard)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp)
            .clip(shape)
            .background(accountBrush(colorKey))
            .drawBehind {
                drawCircle(
                    Color.White.copy(alpha = 0.07f),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.9f, -size.height * 0.1f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.05f),
                    radius = size.height * 0.6f,
                    center = Offset(size.width * 0.1f, size.height * 1.1f)
                )
            }
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.CreditCard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = name.ifBlank { "نام حساب" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("فعال", color = Color.White, fontSize = 10.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                text = "•••• " + last4.ifBlank { "0000" }.toPersianDigits(),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                AnimatedMoney(balance, color = Color.White, fontSize = 20.sp)
                Text("تومان", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
    }
}
