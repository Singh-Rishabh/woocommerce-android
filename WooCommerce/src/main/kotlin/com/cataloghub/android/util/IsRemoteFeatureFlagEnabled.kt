package com.cataloghub.android.util

import com.cataloghub.android.config.WPComRemoteFeatureFlagRepository
import com.cataloghub.android.util.RemoteFeatureFlag.LOCAL_NOTIFICATION_1D_AFTER_FREE_TRIAL_EXPIRES
import com.cataloghub.android.util.RemoteFeatureFlag.LOCAL_NOTIFICATION_1D_BEFORE_FREE_TRIAL_EXPIRES
import com.cataloghub.android.util.RemoteFeatureFlag.LOCAL_NOTIFICATION_STORE_CREATION_READY
import com.cataloghub.android.util.RemoteFeatureFlag.WOO_POS
import javax.inject.Inject

class IsRemoteFeatureFlagEnabled @Inject constructor(
    private val wpComRemoteFeatureFlagRepository: WPComRemoteFeatureFlagRepository
) {
    suspend operator fun invoke(featureFlag: RemoteFeatureFlag): Boolean {
        return when (featureFlag) {
            LOCAL_NOTIFICATION_STORE_CREATION_READY,
            LOCAL_NOTIFICATION_1D_BEFORE_FREE_TRIAL_EXPIRES,
            LOCAL_NOTIFICATION_1D_AFTER_FREE_TRIAL_EXPIRES,
            WOO_POS ->
                PackageUtils.isDebugBuild() ||
                    wpComRemoteFeatureFlagRepository.isRemoteFeatureFlagEnabled(featureFlag.remoteKey)
        }
    }
}
