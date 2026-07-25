package org.octavius.modules.asian.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.octavius.modules.asian.model.PublicationAddRequest
import org.octavius.modules.asian.model.PublicationAddResponse
import org.octavius.modules.asian.model.PublicationCheckRequest
import org.octavius.modules.asian.model.PublicationCheckResponse

object ApiClient {

    private const val BASE_URL = "http://localhost:8080"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun addPublication(request: PublicationAddRequest): PublicationAddResponse {
        return try {
            client.post("$BASE_URL/api/asian-media/add") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Błąd API: ${e.message}")
            PublicationAddResponse(
                success = false,
                message = "Nie można połączyć się z serwerem Augustus. Upewnij się, że aplikacja jest uruchomiona."
            )
        }
    }

    suspend fun checkPublicationExistence(request: PublicationCheckRequest): PublicationCheckResponse {
        return try {
            client.post("$BASE_URL/api/asian-media/check") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Błąd API (/check): ${e.message}")
            PublicationCheckResponse(found = false)
        }
    }
}