package com.cataloghub.android.cardreader.connection.event

sealed class SoftwareUpdateAvailability {
    object Available : SoftwareUpdateAvailability()
    object NotAvailable : SoftwareUpdateAvailability()
}
