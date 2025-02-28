package org.wordpress.android.fluxc.store

import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpapi.jetpack.JetpackWPAPIRestClient
import org.wordpress.android.fluxc.tools.initCoroutineEngine

@RunWith(MockitoJUnitRunner::class)
class JetpackStoreTest {
    @Mock private lateinit var jetpackWPAPIRestClient: JetpackWPAPIRestClient
    @Mock private lateinit var site: SiteModel
    private lateinit var jetpackStore: JetpackStore

    @Before
    fun setUp() {
        jetpackStore = JetpackStore(
            jetpackWPAPIRestClient,
            initCoroutineEngine()
        )
        val siteId = 1
        whenever(site.id).thenReturn(siteId)
    }
}
