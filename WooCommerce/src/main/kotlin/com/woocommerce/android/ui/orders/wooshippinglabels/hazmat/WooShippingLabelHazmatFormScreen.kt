package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.woocommerce.android.ui.compose.component.WCColoredButton

@Composable
fun WooShippingLabelHazmatFormScreen(
    containsHazmatChecked: Boolean,
    onContainsHazmatChanged: (Boolean) -> Unit,
    onSelectCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text("Are you shipping dangerous goods or hazardous materials?")
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Contains hazardous materials",
                modifier = modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            )
            Checkbox(
                checked = containsHazmatChecked,
                onCheckedChange = onContainsHazmatChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        WCColoredButton(
            text = "Select Category",
            onClick = onSelectCategoryClick,
            enabled = containsHazmatChecked
        )
        HorizontalDivider()
        Text("Hazmat details and info")
    }
}
