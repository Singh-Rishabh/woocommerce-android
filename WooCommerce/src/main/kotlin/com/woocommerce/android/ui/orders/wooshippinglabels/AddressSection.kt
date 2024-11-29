package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.woocommerce.android.R
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.AmbiguousLocation
import com.woocommerce.android.model.Location
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress

@Composable
@Suppress("DestructuringDeclarationWithTooManyEntries")
internal fun AddressSectionPortrait(
    shippingAddresses: WooShippingAddresses,
    originAddresses: List<OriginShippingAddress>,
    onShippingFromAddressChange: (OriginShippingAddress) -> Unit,
    onShippingToAddressChange: (Address) -> Unit,
    modifier: Modifier = Modifier
) {
    RoundedCornerBoxWithBorder(modifier.fillMaxWidth()) {
        ConstraintLayout {
            val (
                shipFromLabel,
                shipFromValue,
                shipFromSelect,
                shipToLabel,
                shipToValue,
                shipToEdit,
                divider
            ) = createRefs()

            val barrier = createEndBarrier(shipFromLabel, shipToLabel)
            var expanded by remember { mutableStateOf(false) }

            Text(
                text = stringResource(id = R.string.orderdetail_shipping_label_item_shipfrom),
                modifier = Modifier
                    .constrainAs(shipFromLabel) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .padding(
                        start = dimensionResource(R.dimen.major_100),
                        top = dimensionResource(R.dimen.major_100),
                        bottom = dimensionResource(R.dimen.major_100)
                    )

            )
            Text(
                text = shippingAddresses.shipFrom.toShippingFromString().uppercase(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .constrainAs(shipFromValue) {
                        top.linkTo(shipFromLabel.top)
                        start.linkTo(shipFromLabel.end)
                        end.linkTo(shipFromSelect.start)
                        width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                    }
                    .padding(
                        top = dimensionResource(R.dimen.major_100),
                        bottom = dimensionResource(R.dimen.major_100),
                        start = dimensionResource(R.dimen.major_100),
                        end = dimensionResource(R.dimen.minor_100)
                    )
            )
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .constrainAs(shipFromSelect) {
                        top.linkTo(shipFromLabel.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(shipFromLabel.bottom)
                    }
                    .padding(
                        end = dimensionResource(R.dimen.minor_100)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.sizeIn(minWidth = 150.dp)
                ) {
                    originAddresses.forEach { option ->
                        DropdownMenuItem(onClick = {
                            onShippingFromAddressChange(option)
                            expanded = false
                        }) {
                            Text(
                                text = option.toShippingFromString().uppercase(),
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            Divider(
                modifier = Modifier.constrainAs(divider) {
                    top.linkTo(shipFromLabel.bottom)
                    start.linkTo(parent.start)
                }
            )
            Text(
                text = stringResource(id = R.string.orderdetail_shipping_label_item_shipto),
                modifier = Modifier
                    .constrainAs(shipToLabel) {
                        top.linkTo(divider.bottom)
                        start.linkTo(shipFromLabel.start)
                    }
                    .padding(
                        start = dimensionResource(R.dimen.major_100),
                        top = dimensionResource(R.dimen.major_100),
                        bottom = dimensionResource(R.dimen.major_100)
                    )
            )
            shippingAddresses.shipTo?.let { shipTo ->
                Text(
                    text = shipTo.toString(),
                    modifier = Modifier
                        .constrainAs(shipToValue) {
                            top.linkTo(shipToLabel.top)
                            start.linkTo(barrier)
                            end.linkTo(shipToEdit.start)
                            width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                        }
                        .padding(
                            top = dimensionResource(R.dimen.major_100),
                            bottom = dimensionResource(R.dimen.major_100),
                            start = dimensionResource(R.dimen.major_100),
                            end = dimensionResource(R.dimen.minor_100)
                        )
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .constrainAs(shipToEdit) {
                        top.linkTo(shipToLabel.top)
                        end.linkTo(parent.end)
                    }
                    .padding(
                        top = dimensionResource(R.dimen.major_100),
                        end = dimensionResource(R.dimen.minor_100)
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_pencil),
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddressSectionPortraitPreview() {
    WooThemeWithBackground {
        Box(modifier = Modifier.padding(dimensionResource(R.dimen.major_100))) {
            AddressSectionPortrait(
                shippingAddresses = WooShippingAddresses(
                    shipFrom = getShipFrom(),
                    shipTo = getShipTo(),
                    originAddresses = listOf(getShipFrom())
                ),
                originAddresses = listOf(getShipFrom()),
                onShippingFromAddressChange = {},
                onShippingToAddressChange = {}
            )
        }
    }
}

@Composable
internal fun AddressSectionLandscape(
    shippingAddresses: WooShippingAddresses,
    onShippingFromAddressChange: (OriginShippingAddress) -> Unit,
    onShippingToAddressChange: (Address) -> Unit,
    modifier: Modifier = Modifier
) {
    RoundedCornerBoxWithBorder(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            ShipFromSelection(
                shipFrom = shippingAddresses.shipFrom,
                originAddresses = shippingAddresses.originAddresses,
                onShippingFromAddressChange = onShippingFromAddressChange,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            Row(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.orderdetail_shipping_label_item_shipto),
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(R.dimen.major_100),
                            top = dimensionResource(R.dimen.major_100),
                            bottom = dimensionResource(R.dimen.major_100)
                        )
                )
                shippingAddresses.shipTo?.let { shipTo ->
                    Text(
                        text = shipTo.toString(),
                        modifier = Modifier
                            .padding(
                                top = dimensionResource(R.dimen.major_100),
                                bottom = dimensionResource(R.dimen.major_100),
                                start = dimensionResource(R.dimen.major_100),
                                end = dimensionResource(R.dimen.minor_100)
                            )
                            .weight(1f)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(R.dimen.minor_100),
                            end = dimensionResource(R.dimen.minor_100)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_pencil),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipFromSelection(
    shipFrom: OriginShippingAddress,
    originAddresses: List<OriginShippingAddress>,
    onShippingFromAddressChange: (OriginShippingAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.orderdetail_shipping_label_item_shipfrom),
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.major_100),
                    top = dimensionResource(R.dimen.major_100),
                    end = dimensionResource(R.dimen.major_100)
                )
        )
        Text(
            text = shipFrom.toShippingFromString().uppercase(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .padding(
                    top = dimensionResource(R.dimen.major_100),
                    end = dimensionResource(R.dimen.major_100),
                    start = dimensionResource(R.dimen.major_100),
                    bottom = dimensionResource(R.dimen.minor_100)
                )
                .weight(1f)
        )
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .padding(
                    top = dimensionResource(R.dimen.minor_50),
                    end = dimensionResource(R.dimen.minor_100)
                )
        ) {
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.sizeIn(minWidth = 150.dp)
            ) {
                originAddresses.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onShippingFromAddressChange(option)
                        expanded = false
                    }) {
                        Text(
                            text = option.toShippingFromString().uppercase(),
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 750, heightDp = 200)
@Composable
private fun AddressSectionLandscapePreview() {
    WooThemeWithBackground {
        Box(modifier = Modifier.padding(dimensionResource(R.dimen.major_100))) {
            AddressSectionLandscape(
                shippingAddresses = WooShippingAddresses(
                    shipFrom = getShipFrom(),
                    shipTo = getShipTo(),
                    originAddresses = listOf(getShipFrom())
                ),
                onShippingFromAddressChange = {},
                onShippingToAddressChange = {}
            )
        }
    }
}

internal fun getShipFrom() = OriginShippingAddress(
    firstName = "first name",
    lastName = "last name",
    company = "Company",
    phone = "",
    address1 = "A huge address that should be truncated",
    address2 = "",
    city = "City",
    postcode = "",
    email = "email",
    country = "USA",
    state = "California",
    id = "id_1",
    isDefault = true,
    isVerified = true
)

internal fun getShipTo() = Address(
    firstName = "first name",
    lastName = "last name",
    company = "Company",
    phone = "",
    address1 = "Another Address",
    address2 = "",
    city = "City",
    postcode = "",
    email = "email",
    country = Location("US", "USA"),
    state = AmbiguousLocation.Defined(Location("CA", "California", "USA"))
)
