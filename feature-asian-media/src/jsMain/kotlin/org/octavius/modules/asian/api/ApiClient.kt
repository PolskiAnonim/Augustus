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
import org.octavius.modules.asian.model.PublicationLinkRequest
import org.octavius.modules.asian.model.PublicationLinkResponse
import org.octavius.modules.asian.model.TitleOpenRequest
import org.octavius.modules.asian.model.TitlesAppendRequest
import org.octavius.modules.asian.model.TitlesAppendResponse

object ApiClient {

    private const val BASE_URL = "http://localhost:8080"
    private const val NO_CONNECTION = "Nie można połączyć się z serwerem Augustus. Upewnij się, że aplikacja jest uruchomiona."

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
            PublicationAddResponse(success = false, message = NO_CONNECTION)
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

    suspend fun linkPublication(request: PublicationLinkRequest): PublicationLinkResponse {
        return try {
            client.post("$BASE_URL/api/asian-media/link") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Błąd API (/link): ${e.message}")
            PublicationLinkResponse(success = false, message = NO_CONNECTION)
        }
    }

    /** Dopisywanie tytułów to dodatek do rozpoznania serii, więc błąd kończy się po prostu zerem. */
    suspend fun appendTitles(request: TitlesAppendRequest): TitlesAppendResponse {
        return try {
            client.post("$BASE_URL/api/asian-media/titles/append") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Błąd API (/titles/append): ${e.message}")
            TitlesAppendResponse(added = 0)
        }
    }

    suspend fun openTitle(request: TitleOpenRequest): Boolean {
        return try {
            client.post("$BASE_URL/api/asian-media/open") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.status.isSuccess()
        } catch (e: Exception) {
            println("Błąd API (/open): ${e.message}")
            false
        }
    }
}
