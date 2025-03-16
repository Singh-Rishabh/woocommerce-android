package com.cataloghub.android.ui.plans.domain

import com.automattic.android.tracks.crashlogging.CrashLogging
import com.cataloghub.android.extensions.clock
import com.cataloghub.android.tools.SelectedSite
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import javax.inject.Inject

class CalculatePlanRemainingPeriod @Inject constructor(
    private val selectedSite: SelectedSite,
    private val crashLogging: CrashLogging,
) {

    operator fun invoke(expirationDate: ZonedDateTime): Period {
        val siteClock = selectedSite.getIfExists()?.clock

        val clock = if (siteClock == null) {
            crashLogging.sendReport(message = "Site is null, which should not happen.")
            Clock.systemDefaultZone()
        } else {
            siteClock
        }

        val currentDateInSiteTimezone = ZonedDateTime.now(clock).toLocalDate()

        return Period.between(
            currentDateInSiteTimezone,
            expirationDate.toLocalDate().minusDays(1)
        )
    }
}
