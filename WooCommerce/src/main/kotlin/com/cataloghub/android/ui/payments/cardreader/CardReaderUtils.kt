package com.cataloghub.android.ui.payments.cardreader

import com.cataloghub.android.R
import com.cataloghub.android.cardreader.connection.CardReader
import com.cataloghub.android.model.UiString
import com.cataloghub.android.ui.payments.tracking.TrackingInfo
import kotlin.math.roundToInt

private const val PERCENT_100 = 100
private fun Float.toPercent(): Int = (this * PERCENT_100).roundToInt()

internal fun CardReader.getReadersBatteryLevel(): UiString? =
    getReadersBatteryLevelPercent()?.let { buildBatteryLevelUiString(it) }

internal fun buildBatteryLevelUiString(batteryLevelPercent: Int) = UiString.UiStringRes(
    R.string.card_reader_detail_connected_battery_percentage,
    listOf(UiString.UiStringText(batteryLevelPercent.toString()))
)

internal fun buildBatteryLevelUiString(batteryLevel: Float) = UiString.UiStringRes(
    R.string.card_reader_detail_connected_battery_percentage,
    listOf(UiString.UiStringText(batteryLevel.toPercent().toString()))
)

internal fun CardReader.getReadersBatteryLevelPercent(): Int? = currentBatteryLevel?.toPercent()

internal val TrackingInfo.cardReaderBatteryLevelPercent: Int?
    get() = cardReaderBatteryLevel?.toPercent()
