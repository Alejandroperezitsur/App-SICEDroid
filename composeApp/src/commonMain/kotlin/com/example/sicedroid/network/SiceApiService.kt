package com.example.sicedroid.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SiceApiService {
    val client = HttpClient {
        install(HttpCookies)
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
            header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            header(HttpHeaders.AcceptLanguage, "es-MX,es;q=0.9,en;q=0.8")
            header(HttpHeaders.CacheControl, "no-cache")
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
        }
    }

    private val baseUrl = getBaseUrl()

    private suspend fun soapRequest(soapBody: String, soapAction: String): String {
        val response: HttpResponse = client.post("$baseUrl/ws/wsalumnos.asmx") {
            header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
            header("SOAPAction", "\"$soapAction\"")
            setBody(soapBody)
        }
        return response.bodyAsText()
    }

    suspend fun acceso(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/accesoLogin")

    suspend fun perfil(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/getAlumnoAcademicoWithLineamiento")

    suspend fun kardex(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/getAllKardexConPromedioByAlumno")

    suspend fun carga(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/getCargaAcademicaByAlumno")

    suspend fun parciales(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/getCalifUnidadesByAlumno")

    suspend fun finales(soapBody: String): String =
        soapRequest(soapBody, "http://tempuri.org/getAllCalifFinalByAlumnos")
}
