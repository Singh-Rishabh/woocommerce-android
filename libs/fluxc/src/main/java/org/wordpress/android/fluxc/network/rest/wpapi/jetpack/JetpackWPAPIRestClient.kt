package org.wordpress.android.fluxc.network.rest.wpapi.jetpack

import com.android.volley.Request
import com.android.volley.RequestQueue
import kotlinx.coroutines.suspendCancellableCoroutine
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.Payload
import org.wordpress.android.fluxc.generated.endpoint.JPAPI
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.jetpack.JetpackUser
import org.wordpress.android.fluxc.network.BaseRequest.BaseNetworkError
import org.wordpress.android.fluxc.network.RawRequest
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.rest.wpapi.BaseWPAPIRestClient
import org.wordpress.android.fluxc.network.rest.wpapi.CookieNonceAuthenticator
import org.wordpress.android.fluxc.network.rest.wpapi.WPAPIGsonRequestBuilder
import org.wordpress.android.fluxc.network.rest.wpapi.WPAPIResponse
import org.wordpress.android.fluxc.network.rest.wpapi.WPAPIResponse.Error
import org.wordpress.android.fluxc.network.rest.wpapi.WPAPIResponse.Success
import org.wordpress.android.fluxc.network.rest.wpapi.applicationpasswords.ApplicationPasswordsNetwork
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class JetpackWPAPIRestClient @Inject constructor(
    private val wpApiGsonRequestBuilder: WPAPIGsonRequestBuilder,
    private val cookieNonceAuthenticator: CookieNonceAuthenticator,
    private val applicationPasswordsNetwork: ApplicationPasswordsNetwork,
    dispatcher: Dispatcher,
    @Named("custom-ssl") requestQueue: RequestQueue,
    @Named("no-redirects") private val noRedirectsRequestQueue: RequestQueue,
    userAgent: UserAgent
) : BaseWPAPIRestClient(dispatcher, requestQueue, userAgent) {
    suspend fun fetchJetpackConnectionUrl(
        site: SiteModel,
        useApplicationPasswords: Boolean = false
    ): JetpackWPAPIPayload<String> {
        val response = makeWPAPIRequest<String>(
            site = site,
            path = JPAPI.connection.url.pathV4,
            useApplicationPasswords = useApplicationPasswords
        )

        return when (response) {
            is Success<String> -> JetpackWPAPIPayload(response.data)
            is Error -> JetpackWPAPIPayload(response.error)
        }
    }

    suspend fun registerJetpackSite(registrationUrl: String): Result<String> {
        @Suppress("MagicNumber")
        fun Int.isRedirect(): Boolean = this in 300..399
        return suspendCancellableCoroutine { cont ->
            val request = RawRequest(
                method = Request.Method.GET,
                url = registrationUrl,
                listener = {
                    cont.resume(Result.failure(Exception("Got a success response instead of the expected redirect")))
                },
                onErrorListener = { error ->
                    val response = error.volleyError.networkResponse

                    if (response == null || !response.statusCode.isRedirect()) {
                        cont.resume(Result.failure(error.volleyError))
                        return@RawRequest
                    }

                    response.headers?.get("Location")?.let {
                        if (it.isNotEmpty()) {
                            cont.resume(Result.success(it))
                        } else {
                            cont.resume(Result.failure(Exception("Location header missing")))
                        }
                    }
                }
            )

            noRedirectsRequestQueue.add(request)

            cont.invokeOnCancellation {
                request.cancel()
            }
        }
    }

    suspend fun fetchJetpackUser(
        site: SiteModel,
        useApplicationPasswords: Boolean = false
    ): JetpackWPAPIPayload<JetpackUser> {
        val response = makeWPAPIRequest<JetpackConnectionDataResponse>(
            site = site,
            path = JPAPI.connection.data.pathV4,
            useApplicationPasswords = useApplicationPasswords
        )

        return when (response) {
            is Success<JetpackConnectionDataResponse> -> JetpackWPAPIPayload(
                    response.data?.toJetpackUser()
            )

            is Error -> JetpackWPAPIPayload(response.error)
        }
    }

    private suspend inline fun <reified T> makeWPAPIRequest(
        site: SiteModel,
        path: String,
        useApplicationPasswords: Boolean
    ): WPAPIResponse<T> {
        return if (useApplicationPasswords) {
            applicationPasswordsNetwork.executeGetGsonRequest(
                site = site,
                path = path,
                clazz = T::class.java
            )
        } else {
            val url = site.buildUrl(path)
            cookieNonceAuthenticator.makeAuthenticatedWPAPIRequest(site) { nonce ->
                wpApiGsonRequestBuilder.syncGetRequest(
                    restClient = this,
                    url = url,
                    nonce = nonce.value,
                    clazz = T::class.java
                )
            }
        }
    }

    private fun JetpackConnectionDataResponse.toJetpackUser(): JetpackUser {
        return JetpackUser(
            isConnected = currentUser.isConnected ?: false,
            isMaster = currentUser.isMaster ?: false,
            username = currentUser.username.orEmpty(),
            wpcomEmail = currentUser.wpcomUser?.email.orEmpty(),
            wpcomId = currentUser.wpcomUser?.id ?: 0L,
            wpcomUsername = currentUser.wpcomUser?.login.orEmpty()
        )
    }

    private fun SiteModel.buildUrl(path: String): String {
        val baseUrl = wpApiRestUrl ?: "${url}/wp-json"
        return "${baseUrl.trimEnd('/')}/${path.trimStart('/')}"
    }

    data class JetpackWPAPIPayload<T>(
        val result: T?
    ) : Payload<BaseNetworkError?>() {
        constructor(error: BaseNetworkError) : this(null) {
            this.error = error
        }
    }
}
