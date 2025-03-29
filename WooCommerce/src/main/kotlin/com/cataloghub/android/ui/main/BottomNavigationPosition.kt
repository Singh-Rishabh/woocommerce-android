package com.cataloghub.android.ui.main

import com.cataloghub.android.R
import java.util.*

const val MY_STORE_POSITION = 0
const val ORDERS_POSITION = 1
const val CATEGORIES_POSITION = 2
const val PRODUCTS_POSITION = 3
const val MORE_POSITION = 4

enum class BottomNavigationPosition(val position: Int, val id: Int) {
    MY_STORE(MY_STORE_POSITION, R.id.dashboard),
    ORDERS(ORDERS_POSITION, R.id.orders),
    CATEGORIES(CATEGORIES_POSITION, R.id.categories),
    PRODUCTS(PRODUCTS_POSITION, R.id.products),
    MORE(MORE_POSITION, R.id.moreMenu);

}

fun findNavigationPositionById(id: Int): BottomNavigationPosition = when (id) {
    BottomNavigationPosition.MY_STORE.id -> BottomNavigationPosition.MY_STORE
    BottomNavigationPosition.ORDERS.id -> BottomNavigationPosition.ORDERS
    BottomNavigationPosition.CATEGORIES.id -> BottomNavigationPosition.CATEGORIES
    BottomNavigationPosition.PRODUCTS.id -> BottomNavigationPosition.PRODUCTS
    BottomNavigationPosition.MORE.id -> BottomNavigationPosition.MORE
    else -> BottomNavigationPosition.MY_STORE
}

fun BottomNavigationPosition.getTag(): String = id.toString()
