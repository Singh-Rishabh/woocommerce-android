package com.cataloghub.android.ui.plans.domain

import com.automattic.android.tracks.crashlogging.CrashLogging
import com.cataloghub.android.tools.SelectedSite
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculatePlanRemainingPeriodTest {

    private val selectedSite: SelectedSite = mock()
    private val crashLogging: CrashLogging = mock()

    private val sut = CalculatePlanRemainingPeriod(selectedSite, crashLogging)

    @Test
    fun `given the scenario where site does not exist, should not crash and report event`() {
        // given
        selectedSite.stub { on { getIfExists() }.thenReturn(null) }
        val expirationDate =
            ZonedDateTime.of(2023, 10, 10, 0, 0, 0, 0, ZoneId.of("UTC"))

        // when
        sut(expirationDate)

        // then
        verify(crashLogging).sendReport(message = "Site is null, which should not happen.")
    }
}
