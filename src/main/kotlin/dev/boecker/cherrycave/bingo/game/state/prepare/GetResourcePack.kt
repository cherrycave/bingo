package dev.boecker.cherrycave.bingo.game.state.prepare

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.bukkit.Material

val resourcePackGenUrl = System.getenv("RP_GEN_URL") ?: "http://localhost:3000"

private val ktorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
private data class GenerateResourcePackRequest(
    val materials: List<String>,
    val boardSize: Int,
)

@Serializable
data class GenerateResourcePackResponse(
    val hash: String,
    val downloadUrl: String,
)

suspend fun getResourcePackForMaterials(materials: List<Material>, boardSize: Int): Pair<String, String> {
    val generateResponse = ktorClient.post("${resourcePackGenUrl}/generate") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(GenerateResourcePackRequest(
            materials = materials.map { it.name.lowercase() },
            boardSize = boardSize
        ))
    }

    if (generateResponse.status != HttpStatusCode.OK) {
        error("Error when requesting resource pack generation: ${generateResponse.status}")
    }

    val generatedResourcePack = generateResponse.body<GenerateResourcePackResponse>()

    return generatedResourcePack.hash to generatedResourcePack.downloadUrl
}
