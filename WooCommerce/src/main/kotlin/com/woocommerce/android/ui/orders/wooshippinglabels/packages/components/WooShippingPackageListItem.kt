package com.woocommerce.android.ui.orders.wooshippinglabels.packages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.SelectionCheck
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData

@Composable
fun WooSavedPackageListItem(
    modifier: Modifier,
    packageData: PackageData,
    onPackageSelected: (PackageData, Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onPackageSelected(packageData, packageData.isSelected.not()) }
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionCheck(
                isSelected = packageData.isSelected,
                onSelectionChange = { onPackageSelected(packageData, it) }
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = packageData.description,
                    style = MaterialTheme.typography.caption,
                    color = colorResource(id = R.color.color_on_surface_disabled)
                )
                Text(
                    text = packageData.name,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = packageData.dimensions,
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Divider()
    }
}
