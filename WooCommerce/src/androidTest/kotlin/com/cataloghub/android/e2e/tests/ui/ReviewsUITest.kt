@file:Suppress("DEPRECATION")

package com.cataloghub.android.e2e.tests.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.ActivityTestRule
import com.cataloghub.android.BuildConfig
import com.cataloghub.android.e2e.helpers.InitializationRule
import com.cataloghub.android.e2e.helpers.TestBase
import com.cataloghub.android.e2e.helpers.util.MocksReader
import com.cataloghub.android.e2e.helpers.util.ReviewData
import com.cataloghub.android.e2e.helpers.util.iterator
import com.cataloghub.android.e2e.rules.Retry
import com.cataloghub.android.e2e.rules.RetryTestRule
import com.cataloghub.android.e2e.screens.TabNavComponent
import com.cataloghub.android.e2e.screens.login.WelcomeScreen
import com.cataloghub.android.e2e.screens.reviews.ReviewsListScreen
import com.cataloghub.android.ui.login.LoginActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ReviewsUITest : TestBase(failOnUnmatchedWireMockRequests = false) {
    @get:Rule(order = 0)
    val rule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 2)
    val initRule = InitializationRule()

    @get:Rule(order = 3)
    var activityRule = ActivityTestRule(LoginActivity::class.java)

    @get:Rule(order = 4)
    var retryTestRule = RetryTestRule()

    @Before
    fun setUp() {
        WelcomeScreen
            .skipCarouselIfNeeded()
            .selectLogin()
            .proceedWith(BuildConfig.SCREENSHOTS_URL)
            .proceedWith(BuildConfig.SCREENSHOTS_USERNAME)
            .proceedWith(BuildConfig.SCREENSHOTS_PASSWORD)

        TabNavComponent()
            .gotoMoreMenuScreen()
            .openReviewsListScreen(composeTestRule)
    }

    @Retry(numberOfTimes = 1)
    @Test
    fun e2eReviewListShowsAllReviews() {
        val reviewsJSONArray = MocksReader().readAllReviewsToArray()

        reviewsJSONArray.iterator().forEach { review ->
            val currentReview = ReviewData(
                review.getInt("product_id"),
                review.getString("status"),
                review.getString("reviewer"),
                review.getString("review"),
                review.getInt("rating")
            )

            ReviewsListScreen()
                .scrollToReview(currentReview.title)
                .assertReviewCard(currentReview)
                .selectReviewByTitle(currentReview.title)
                .assertSingleReviewScreen(currentReview)
                .goBackToReviewsScreen()
        }
    }
}
