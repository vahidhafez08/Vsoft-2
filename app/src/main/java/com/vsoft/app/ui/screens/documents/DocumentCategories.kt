package com.vsoft.app.ui.screens.documents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.WarningOrange

data class DocumentCategoryStyle(val title: String, val icon: ImageVector, val color: Color)

object DocumentCategories {
    val all = listOf(
        DocumentCategoryStyle("هویتی", Icons.Filled.Badge, BrandBlue),
        DocumentCategoryStyle("قرارداد", Icons.Filled.Description, BrandPurple),
        DocumentCategoryStyle("فاکتور", Icons.Filled.Receipt, ExpenseRed),
        DocumentCategoryStyle("بانکی", Icons.Filled.AccountBalance, IncomeGreen),
        DocumentCategoryStyle("سایر", Icons.Filled.Folder, WarningOrange)
    )

    fun title(category: Int): String = all.getOrNull(category)?.title ?: all.last().title
    fun icon(category: Int): ImageVector = all.getOrNull(category)?.icon ?: Icons.Filled.Folder
    fun color(category: Int): Color = all.getOrNull(category)?.color ?: WarningOrange
}
