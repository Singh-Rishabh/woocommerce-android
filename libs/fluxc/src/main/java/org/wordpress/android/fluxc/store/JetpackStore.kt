package org.wordpress.android.fluxc.store

import com.android.volley.VolleyError
import org.wordpress.android.fluxc.Payload
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.jetpack.JetpackUser
import org.wordpress.android.fluxc.network.rest.wpapi.jetpack.JetpackWPAPIRestClient
import org.wordpress.android.fluxc.store.Store.OnChangedError
import org.wordpress.android.fluxc.tools.CoroutineEngine
import org.wordpress.android.util.AppLog.T
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

private const val JETPACK_DOMAIN = "jetpack.wordpress.com"

@Singleton
class JetpackStore @Inject constructor(
    private val jetpackWPAPIRestClient: JetpackWPAPIRestClient,
    private val coroutineEngine: CoroutineEngine
) {
    suspend fun fetchJetpackConnectionUrl(
        site: SiteModel,
        useApplicationPasswords: Boolean,
        autoRegisterSiteIfNeeded: Boolean,
    ): JetpackResult<String> {
        if (site.isUsingWpComRestApi) error("This function supports only self-hosted site using WPAPI")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackConnectionUrl") {
            val result = jetpackWPAPIRestClient.fetchJetpackConnectionUrl(site, useApplicationPasswords)

            result.toJetpackResult { result ->
                val url = result.trim('"').replace("\\", "")
                val connectionUri = URI.create(url)
                if (!autoRegisterSiteIfNeeded || connectionUri.host == JETPACK_DOMAIN || useApplicationPasswords) {
                    JetpackResult(url)
                } else {
                    registerJetpackSite(url).fold(
                        onSuccess = {
                            JetpackResult(it)
                        },
                        onFailure = {
                            val errorCode = (it as? VolleyError)?.networkResponse?.statusCode
                            JetpackResult(
                                JetpackError(
                                    message = it.message,
                                    errorCode = errorCode
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    private suspend fun registerJetpackSite(registrationUrl: String): Result<String> =
        jetpackWPAPIRestClient.registerJetpackSite(registrationUrl)

    suspend fun fetchJetpackUser(
        site: SiteModel,
        useApplicationPasswords: Boolean
    ): JetpackResult<JetpackUser> {
        if (site.isUsingWpComRestApi) error("This function is not implemented yet for Jetpack tunnel")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackUser") {
            val result = jetpackWPAPIRestClient.fetchJetpackUser(site, useApplicationPasswords)

            result.toJetpackResult { result ->
                JetpackResult(result)
            }
        }
    }

    data class JetpackResult<T>(
        val data: T?
    ) : Payload<JetpackError>() {
        constructor(error: JetpackError) : this(null) {
            this.error = error
        }
    }

    data class JetpackError(
        val message: String? = null,
        val errorCode: Int? = null
    ) : OnChangedError

    private suspend inline fun <T, R> JetpackWPAPIRestClient.JetpackWPAPIPayload<T>.toJetpackResult(
        transform: suspend (T) -> JetpackResult<R>
    ): JetpackResult<R> {
        return when {
            isError -> JetpackResult(JetpackError(error?.message, error?.volleyError?.networkResponse?.statusCode))
            result == null -> JetpackResult(JetpackError("Response Empty"))
            else -> transform(result)
        }
    }
}
