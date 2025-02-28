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
        autoRegisterSiteIfNeeded: Boolean = false,
        useApplicationPasswords: Boolean = false
    ): JetpackResult<String> {
        if (site.isUsingWpComRestApi) error("This function supports only self-hosted site using WPAPI")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackConnectionUrl") {
            val result = jetpackWPAPIRestClient.fetchJetpackConnectionUrl(site, useApplicationPasswords)

            when {
                result.isError -> JetpackResult(
                    JetpackError(
                        message = result.error?.message,
                        errorCode = result.error?.volleyError?.networkResponse?.statusCode
                    )
                )

                result.result.isNullOrEmpty() -> JetpackResult(
                    JetpackError("Response Empty")
                )

                else -> {
                    val url = result.result.trim('"').replace("\\", "")
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
    }

    private suspend fun registerJetpackSite(registrationUrl: String): Result<String> =
        jetpackWPAPIRestClient.registerJetpackSite(registrationUrl)

    suspend fun fetchJetpackUser(
        site: SiteModel,
        useApplicationPasswords: Boolean = false
    ): JetpackResult<JetpackUser> {
        if (site.isUsingWpComRestApi) error("This function is not implemented yet for Jetpack tunnel")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackUser") {
            val result = jetpackWPAPIRestClient.fetchJetpackUser(site, useApplicationPasswords)

            when {
                result.isError -> JetpackResult(
                    JetpackError(
                        message = result.error?.message,
                        errorCode = result.error?.volleyError?.networkResponse?.statusCode
                    )
                )

                result.result == null -> JetpackResult(
                    JetpackError("Response Empty")
                )

                else -> {
                    JetpackResult(result.result)
                }
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
}
