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
    private val coroutineEngine: CoroutineEngine,
) {
    suspend fun fetchJetpackConnectionUrl(
        site: SiteModel,
        autoRegisterSiteIfNeeded: Boolean = false,
        useApplicationPasswords: Boolean = false
    ): JetpackConnectionUrlResult {
        if (site.isUsingWpComRestApi) error("This function supports only self-hosted site using WPAPI")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackConnectionUrl") {
            val result = jetpackWPAPIRestClient.fetchJetpackConnectionUrl(site, useApplicationPasswords)

            when {
                result.isError -> JetpackConnectionUrlResult(
                    JetpackConnectionUrlError(
                        message = result.error?.message,
                        errorCode = result.error?.volleyError?.networkResponse?.statusCode
                    )
                )

                result.result.isNullOrEmpty() -> JetpackConnectionUrlResult(
                    JetpackConnectionUrlError("Response Empty")
                )

                else -> {
                    val url = result.result.trim('"').replace("\\", "")
                    val connectionUri = URI.create(url)
                    if (!autoRegisterSiteIfNeeded || connectionUri.host == JETPACK_DOMAIN || useApplicationPasswords) {
                        JetpackConnectionUrlResult(url)
                    } else {
                        registerJetpackSite(url).fold(
                            onSuccess = {
                                JetpackConnectionUrlResult(it)
                            },
                            onFailure = {
                                val errorCode = (it as? VolleyError)?.networkResponse?.statusCode
                                JetpackConnectionUrlResult(
                                    JetpackConnectionUrlError(
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

    data class JetpackConnectionUrlResult(
        val url: String
    ) : Payload<JetpackConnectionUrlError>() {
        constructor(error: JetpackConnectionUrlError) : this("") {
            this.error = error
        }
    }

    class JetpackConnectionUrlError(
        val message: String? = null,
        val errorCode: Int? = null
    ) : OnChangedError

    suspend fun fetchJetpackUser(site: SiteModel, useApplicationPasswords: Boolean = false): JetpackUserResult {
        if (site.isUsingWpComRestApi) error("This function is not implemented yet for Jetpack tunnel")
        return coroutineEngine.withDefaultContext(T.API, this, "fetchJetpackUser") {
            val result = jetpackWPAPIRestClient.fetchJetpackUser(site, useApplicationPasswords)

            when {
                result.isError -> JetpackUserResult(
                    JetpackUserError(
                        message = result.error?.message,
                        errorCode = result.error?.volleyError?.networkResponse?.statusCode
                    )
                )

                result.result == null -> JetpackUserResult(
                    JetpackUserError("Response Empty")
                )

                else -> {
                    JetpackUserResult(result.result)
                }
            }
        }
    }

    data class JetpackUserResult(
        val user: JetpackUser?
    ) : Payload<JetpackUserError>() {
        constructor(error: JetpackUserError) : this(null) {
            this.error = error
        }
    }

    class JetpackUserError(
        val message: String? = null,
        val errorCode: Int? = null
    ) : OnChangedError
}
