package com.example.sicedroid.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SiceApiService {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx"

    suspend fun acceso(soapBody: String): String {
        val response: HttpResponse = client.post("$baseUrl/ws/wsalumnos.asmx") {
            header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
            header("SOAPAction", "\"http://tempuri.org/accesoLogin\"")
            setBody(soapBody)
        }
        return response.bodyAsText()
    }

    suspend fun perfil(soapBody: String): String {
        val response: HttpResponse = client.post("$baseUrl/ws/wsalumnos.asmx") {
            header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
            header("SOAPAction", "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"")
            setBody(soapBody)
        }
        return response.bodyAsText()
    }
}
