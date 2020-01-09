package service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import model.*
import org.jetbrains.exposed.sql.*

class EchoService {

    private val mUrl = "https://postman-echo.com/get?foo1=bar1&foo2=bar2"

    suspend fun getEcho() : String{
        return httpClient.use { client ->
            client.get(mUrl)
        }
    }

    private val httpClient = HttpClient(Apache) {
        engine {
            followRedirects = true
            socketTimeout = 10_000
            connectTimeout = 10_000
            connectionRequestTimeout = 20_000
            customizeClient {
                setMaxConnTotal(10)  //setMaxConnTotal(1000)
                setMaxConnPerRoute(5)  //setMaxConnPerRoute(100)
            }
        }
        install(DefaultRequest) {
            // headers.appendAuth()
        }
        install(JsonFeature) {
            serializer = GsonSerializer {
                // serializeNulls()
                disableHtmlEscaping()
            }
        }
    }


}
