package com.example.butcetakip.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.butcetakip.data.Category

fun getCategoryIcon(category: Category): ImageVector {
    return when(category) {
        Category.SALARY -> Icons.Rounded.AccountBalanceWallet
        Category.FREELANCE -> Icons.Rounded.Computer
        Category.INVESTMENT_INCOME -> Icons.Rounded.TrendingUp
        Category.SCHOLARSHIP -> Icons.Rounded.School
        Category.POCKET_MONEY -> Icons.Rounded.Money
        Category.OTHER_INCOME -> Icons.Rounded.AddCircle
        
        Category.FOOD -> Icons.Rounded.Restaurant
        Category.TRANSPORT -> Icons.Rounded.DirectionsCar
        Category.RENT -> Icons.Rounded.Home
        Category.SHOPPING -> Icons.Rounded.ShoppingCart
        Category.BILLS -> Icons.Rounded.Receipt
        Category.SUBSCRIPTION -> Icons.Rounded.Autorenew
        Category.INVESTMENT_EXPENSE -> Icons.Rounded.TrendingDown
        Category.HEALTH -> Icons.Rounded.LocalHospital
        Category.ENTERTAINMENT -> Icons.Rounded.Movie
        Category.EDUCATION -> Icons.Rounded.MenuBook
        Category.OTHER_EXPENSE -> Icons.Rounded.MoreHoriz
        Category.CUSTOM -> Icons.Rounded.Star
    }
}

fun getCustomIconByName(name: String): ImageVector {
    return when(name) {
        "Star" -> Icons.Rounded.Star
        "Favorite" -> Icons.Rounded.Favorite
        "ShoppingCart" -> Icons.Rounded.ShoppingCart
        "Home" -> Icons.Rounded.Home
        "Build" -> Icons.Rounded.Build
        "CardTravel" -> Icons.Rounded.CardTravel
        "Pets" -> Icons.Rounded.Pets
        "Flight" -> Icons.Rounded.Flight
        else -> Icons.Rounded.Star
    }
}
