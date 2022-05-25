package no.nav.helsearbeidsgiver.aktor

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.tokenprovider.AccessTokenProvider
import org.slf4j.LoggerFactory
import java.net.ConnectException

open class AktørException(message: String, causedBy: Exception?) : RuntimeException(message, causedBy)
open class FantIkkeAktørException(causedBy: java.lang.Exception?) : AktørException("Fant ikke aktørId", causedBy)
open class AktørKallResponseException(statusCode: Int, causedBy: java.lang.Exception?) : AktørException("Kall mot aktørregister for aktørId feiler med http status $statusCode", causedBy)

class AktorClient(
    private val tokenConsumer: AccessTokenProvider,
    private val username: String,
    private val endpointUrl: String,
    private val httpClient: HttpClient
) {
    private val log = LoggerFactory.getLogger(AktorClient::class.java)

    @Throws(AktørException::class)
    fun getAktorId(fnr: String): String {
        return getIdent(fnr, "AktoerId")
    }

    @Throws(AktørException::class)
    private fun getIdent(sokeIdent: String, identgruppe: String): String {
        var aktor: Aktor? = null

        runBlocking {
            val urlString = "$endpointUrl/identer?gjeldende=true&identgruppe=$identgruppe"
            try {
                aktor = httpClient.get<AktorResponse> {
                    url(urlString)
                    header("Authorization", "Bearer ${tokenConsumer.getToken()}")
                    header("Nav-Consumer-Id", "$username")
                    header("Nav-Personidenter", "$sokeIdent")
                }[sokeIdent]
            } catch (cause: ClientRequestException) {
                val status = cause.response?.status?.value
                log.error("Kall mot aktørregister på $endpointUrl feiler med HTTP-$status")
                throw AktørKallResponseException(status, null)
            } catch (cause: ConnectException) {
                log.error("Kall til $urlString gir ${cause.message}")
                throw AktørKallResponseException(999, cause)
            }
            if (aktor?.identer == null) {
                log.error("Fant ikke aktøren: ${aktor?.feilmelding}")
                throw FantIkkeAktørException(null)
            }
        }
        return aktor?.identer?.firstOrNull()?.ident.toString()
    }
}
