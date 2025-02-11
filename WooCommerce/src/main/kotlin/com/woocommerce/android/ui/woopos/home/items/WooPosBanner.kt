package com.woocommerce.android.ui.woopos.home.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosCard
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding

@Composable
@Suppress("DestructuringDeclarationWithTooManyEntries")
fun WooPosBanner(
    title: String,
    message: String,
    bannerIcon: Int,
    onClose: () -> Unit,
    onLearnMore: () -> Unit
) {
    val bannerContentDescription = getGroupedContentDescription(title, message)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 2.dp.toAdaptivePadding(),
                end = 2.dp.toAdaptivePadding(),
                bottom = 16.dp.toAdaptivePadding()
            )
            .semantics {
                contentDescription = bannerContentDescription
            }
            .focusable()
    ) {
        WooPosCard(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(24.dp.toAdaptivePadding())
                    .fillMaxWidth()
            ) {
                val (icon, header, description, close) = createRefs()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .constrainAs(icon) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Icon(
                        painterResource(id = bannerIcon),
                        contentDescription = stringResource(
                            id = R.string.woopos_banner_simple_products_info_content_description
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.87f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(
                            start = 32.dp.toAdaptivePadding(),
                            bottom = 8.dp.toAdaptivePadding()
                        )
                        .constrainAs(header) {
                            top.linkTo(parent.top)
                            start.linkTo(icon.end)
                            end.linkTo(close.start)
                            width = Dimension.fillToConstraints
                        }
                )

                val annotatedText = buildAnnotatedString {
                    append(message)
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(" ")
                        append(stringResource(id = R.string.woopos_banner_simple_products_only_message_learn_more))
                    }
                }

                Box(
                    modifier = Modifier
                        .constrainAs(description) {
                            top.linkTo(header.bottom)
                            start.linkTo(header.start)
                            end.linkTo(close.start)
                            width = Dimension.fillToConstraints
                        }
                        .padding(
                            start = 24.dp.toAdaptivePadding(),
                            end = 18.dp.toAdaptivePadding()
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .clickable {
                                onLearnMore()
                            }
                            .padding(
                                start = 8.dp.toAdaptivePadding(),
                                top = 8.dp.toAdaptivePadding(),
                                bottom = 8.dp.toAdaptivePadding(),
                            ),
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.87f)
                    )
                }

                IconButton(
                    modifier = Modifier
                        .constrainAs(close) {
                            top.linkTo(header.top)
                            bottom.linkTo(header.bottom)
                            end.linkTo(parent.end)
                        },
                    onClick = { onClose() }
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        contentDescription = stringResource(
                            id = R.string.woopos_banner_simple_products_close_content_description
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun getGroupedContentDescription(title: String, message: String): String {
    val bannerContentDescription = stringResource(id = R.string.woopos_banner_simple_products_content_description)
    val learnMoreContentDescription = stringResource(
        id = R.string.woopos_banner_simple_products_learn_more_content_description
    )
    val combinedContentDescription = "$bannerContentDescription\n$title\n$message\n$learnMoreContentDescription"
    return combinedContentDescription
}

@WooPosPreview
@Composable
fun PreviewWooPosBannerScreen() {
    WooPosTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp.toAdaptivePadding()),
            contentAlignment = Alignment.Center
        ) {
            WooPosBanner(
                title = "Showing simple products only",
                message = "Only simple physical products are compatible with POS right now. Other product types," +
                    " such as variable and virtual, will become available in future updates. ",
                bannerIcon = R.drawable.info,
                onClose = { },
                onLearnMore = { }
            )
        }
    }
}
