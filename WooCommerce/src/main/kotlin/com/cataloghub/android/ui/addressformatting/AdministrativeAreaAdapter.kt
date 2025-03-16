package com.cataloghub.android.ui.addressformatting

import com.cataloghub.android.model.AmbiguousLocation
import com.cataloghub.android.ui.orders.details.editing.address.LocationCode

fun AmbiguousLocation.presentationName(countryCode: LocationCode): String =
    this.asLocation().run {
        if (countryCode == "JP" || countryCode == "TR") {
            name
        } else {
            code
        }
    }
