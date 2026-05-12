package com.example.sicedroid.network

import com.example.sicedroid.model.ProfileStudent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SNRepository(private val apiService: SiceApiService) {

    private var userMatricula: String = ""

    private val bodyLogin = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <accesoLogin xmlns="http://tempuri.org/">
              <strMatricula>%s</strMatricula>
              <strContrasenia>%s</strContrasenia>
              <tipoUsuario>ALUMNO</tipoUsuario>
            </accesoLogin>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private val bodyPerfilWithLineamiento = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private fun extractResult(xml: String, tag: String): String? {
        val startTag = "<$tag>"
        val endTag = "</$tag>"
        val start = xml.indexOf(startTag)
        val end = xml.indexOf(endTag)
        if (start != -1 && end != -1) {
            return xml.substring(start + startTag.length, end)
        }
        return null
    }

    suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        try {
            val soapBody = bodyLogin.replace("%s", matricula).replaceFirst("%s", contrasenia)
            val xmlString = apiService.acceso(soapBody)
            val resultJson = extractResult(xmlString, "accesoLoginResult")

            if (resultJson != null) {
                val jsonElement = Json { ignoreUnknownKeys = true }.parseToJsonElement(resultJson)
                val jsonObject = jsonElement.jsonObject
                
                val accesoValue = jsonObject["acceso"]?.jsonPrimitive?.content ?: ""
                
                if (accesoValue.lowercase() == "true" || accesoValue == "1") {
                    userMatricula = matricula
                    return true
                }
                return false
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun profile(matricula: String): ProfileStudent {
        try {
            val xmlString = apiService.perfil(bodyPerfilWithLineamiento)
            var resultText = extractResult(xmlString, "getAlumnoAcademicoWithLineamientoResult")
            
            if (resultText != null) {
                var processed = resultText!!
                while (processed.contains("&lt;")) {
                    processed = processed.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
                }
                
                if (processed.trim().startsWith("{")) {
                    val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(processed.trim()).jsonObject
                    val nombre = json["nombre"]?.jsonPrimitive?.content ?: ""
                    val carrera = json["carrera"]?.jsonPrimitive?.content ?: ""
                    val semestre = json["semActual"]?.jsonPrimitive?.content ?: ""
                    val estatus = json["estatus"]?.jsonPrimitive?.content ?: ""
                    
                    return ProfileStudent(
                        matricula = matricula,
                        nombre = nombre,
                        carrera = carrera,
                        semestre = semestre.toIntOrNull() ?: 0,
                        estatusAlumno = estatus
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return ProfileStudent(matricula = matricula) // Fallback
    }
}
