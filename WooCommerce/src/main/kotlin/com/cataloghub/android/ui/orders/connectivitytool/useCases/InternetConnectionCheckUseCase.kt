package com.cataloghub.android.ui.orders.connectivitytool.useCases

import com.cataloghub.android.tools.NetworkStatus
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.Failure
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.InProgress
import com.cataloghub.android.ui.orders.connectivitytool.ConnectivityCheckStatus.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InternetConnectionCheckUseCase @Inject constructor(
    private val networkStatus: NetworkStatus
) {
    operator fun invoke(): Flow<ConnectivityCheckStatus> = flow {
        emit(InProgress)
        if (networkStatus.isConnected()) {
            emit(Success)
        } else {
            emit(Failure())
        }
    }
}
