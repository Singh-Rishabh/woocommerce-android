package org.wordpress.android.fluxc.store

import org.junit.Ignore
import org.mockito.Mockito.mock
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpapi.jetpack.JetpackWPAPIRestClient
import org.wordpress.android.fluxc.tools.initCoroutineEngine
import kotlin.test.Test

@Suppress("unused")
class JetpackStoreTest {
    private val jetpackWPAPIRestClient: JetpackWPAPIRestClient = mock()
    private val site: SiteModel = SiteModel()

    private val jetpackStore: JetpackStore = JetpackStore(
        jetpackWPAPIRestClient,
        initCoroutineEngine()
    )

    @Test
    @Ignore("Empty test to keep JUnit happy, we'll add real tests later")
    @Suppress("EmptyFunctionBlock")
    fun `empty test to keep JUnit happy`() {

    }
}
