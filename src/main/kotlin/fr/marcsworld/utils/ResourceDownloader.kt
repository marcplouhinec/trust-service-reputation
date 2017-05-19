package fr.marcsworld.utils

import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.HttpClients
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Utility singleton for downloading [Resource]s.
 *
 * @author Marc Plouhinec
 */
object ResourceDownloader {

    private val noopX509TrustManager = NoopX509TrustManager()

    /**
     * Download the document accessible from the given [resource].
     *
     * @param resource [Resource] to the document data.
     * @return Document data.
     */
    fun downloadResource(resource: Resource): ByteArray {
        val resourceUrl = resource.url.toString()

        return if (resource is UrlResource) {
            // Apache HttpClient allows us to ignore problems with SSL certificates and handle redirections
            try {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf<TrustManager>(noopX509TrustManager), java.security.SecureRandom())
                val httpClient = HttpClients
                        .custom()
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setSSLContext(sslContext)
                        .disableAutomaticRetries()
                        .build()
                httpClient.execute(HttpGet(resourceUrl)).use {
                    if (it.statusLine.statusCode != 200) {
                        throw IOException("Resource '$resourceUrl' unavailable: the status code is ${it.statusLine.statusCode} instead of 200.")
                    }
                    it.entity.content.use {
                        it.readBytes()
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        } else {
            resource.inputStream.buffered().use {
                it.readBytes()
            }
        }
    }

    /**
     * [X509TrustManager] that does nothing. It is useful for accepting any SSL certificate
     */
    private class NoopX509TrustManager : X509TrustManager {
        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

}