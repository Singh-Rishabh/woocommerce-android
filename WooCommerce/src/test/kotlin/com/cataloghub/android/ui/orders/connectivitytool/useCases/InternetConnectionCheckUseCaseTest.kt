package com.cataloghub.android.ui.orders.connectivitytool.useCases

import com.cataloghub.android.tools.NetworkStatus
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.Failure
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.InProgress
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.Success
import com.cataloghub.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class InternetConnectionCheckUseCaseTest : BaseUnitTest() {
    private lateinit var sut: InternetConnectionCheckUseCase
    private lateinit var networkStatus: NetworkStatus

    @Before
    fun setUp() {
        networkStatus = mock()
        sut = InternetConnectionCheckUseCase(networkStatus)
    }

    @Test
    fun `when network is connected then emit Success`() = testBlocking {
        // Given
        val stateEvents = mutableListOf<ConnectivityCheckStatus>()
        whenever(networkStatus.isConnected()).thenReturn(true)

        // When
        sut.invoke().onEach {
            stateEvents.add(it)
        }.launchIn(this)

        // Then
        assertThat(stateEvents).isEqualTo(listOf(InProgress, Success))
    }

    @Test
    fun `when network is not connected then emit Failure`() = testBlocking {
        // Given
        val stateEvents = mutableListOf<ConnectivityCheckStatus>()
        whenever(networkStatus.isConnected()).thenReturn(false)

        // When
        sut.invoke().onEach {
            stateEvents.add(it)
        }.launchIn(this)

        // Then
        assertThat(stateEvents).isEqualTo(listOf(InProgress, Failure()))
    }
}
