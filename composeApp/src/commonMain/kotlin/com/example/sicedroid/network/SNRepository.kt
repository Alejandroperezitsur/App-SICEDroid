package com.example.sicedroid.network

import com.example.sicedroid.model.*
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

    private val bodyKardex = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
              <aluLineamiento>%d</aluLineamiento>
            </getAllKardexConPromedioByAlumno>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private val bodyCarga = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private val bodyCalifFinal = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
              <bytModEducativo>%d</bytModEducativo>
            </getAllCalifFinalByAlumnos>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private val bodyCalifUnidades = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

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
            val soapBody = bodyLogin.replaceFirst("%s", matricula).replaceFirst("%s", contrasenia)
            val xmlString = apiService.acceso(soapBody)
            val resultJson = extractResult(xmlString, "accesoLoginResult")

            if (resultJson != null) {
                val jsonObject = json.parseToJsonElement(resultJson).jsonObject
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
            val resultText = extractResult(xmlString, "getAlumnoAcademicoWithLineamientoResult") ?: return ProfileStudent(matricula = matricula)

            var processed = resultText
            while (processed.contains("&lt;")) {
                processed = processed.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
            }

            if (processed.trim().startsWith("{")) {
                val jsonObj = json.parseToJsonElement(processed.trim()).jsonObject
                val nombre = jsonObj["nombre"]?.jsonPrimitive?.content ?: ""
                val carrera = jsonObj["carrera"]?.jsonPrimitive?.content ?: ""
                val semestre = jsonObj["semActual"]?.jsonPrimitive?.content ?: ""
                val estatus = jsonObj["estatus"]?.jsonPrimitive?.content ?: ""
                val especialidad = jsonObj["especialidad"]?.jsonPrimitive?.content ?: ""
                val promedio = jsonObj["promedio"]?.jsonPrimitive?.content ?: ""
                val estado = jsonObj["estado"]?.jsonPrimitive?.content ?: ""
                val inscrito = jsonObj["inscrito"]?.jsonPrimitive?.content ?: ""
                val cdtsReunidos = jsonObj["cdtsReunidos"]?.jsonPrimitive?.content ?: "0"
                val cdtsActuales = jsonObj["cdtsActuales"]?.jsonPrimitive?.content ?: "0"
                val statusMatricula = jsonObj["statusMatricula"]?.jsonPrimitive?.content ?: ""
                val estatusAcademico = jsonObj["estatusAcademico"]?.jsonPrimitive?.content ?: ""
                val fotoUrl = jsonObj["fotoUrl"]?.jsonPrimitive?.content ?: ""
                val sinAdeudos = jsonObj["sinAdeudos"]?.jsonPrimitive?.content ?: ""
                val reinscripcionFecha = jsonObj["reinscripcionFecha"]?.jsonPrimitive?.content ?: ""
                val lineamiento = jsonObj["lineamiento"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                val modEducativo = jsonObj["modEducativo"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1

                return ProfileStudent(
                    matricula = matricula,
                    nombre = clean(nombre),
                    carrera = clean(carrera),
                    semestre = semestre.toIntOrNull() ?: 0,
                    estatusAlumno = estatus,
                    especialidad = especialidad,
                    promedio = promedio,
                    estado = estado,
                    inscrito = inscrito,
                    cdtsReunidos = cdtsReunidos.toIntOrNull() ?: 0,
                    cdtsActuales = cdtsActuales.toIntOrNull() ?: 0,
                    statusMatricula = statusMatricula,
                    estatusAcademico = estatusAcademico,
                    fotoUrl = fotoUrl,
                    sinAdeudos = sinAdeudos,
                    reinscripcionFecha = reinscripcionFecha,
                    lineamiento = lineamiento,
                    modEducativo = modEducativo
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ProfileStudent(matricula = matricula)
    }

    suspend fun getKardex(matricula: String, lineamiento: Int = 1): List<MateriaKardex> {
        return try {
            val soapBody = bodyKardex.replace("%d", lineamiento.toString())
            val xmlString = apiService.kardex(soapBody)
            val result = extractResult(xmlString, "getAllKardexConPromedioByAlumnoResult")
            if (result != null) {
                val responseObj = json.decodeFromString<KardexResponse>(result)
                responseObj.lstKardex
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCarga(matricula: String): List<MateriaCarga> {
        return try {
            val xmlString = apiService.carga(bodyCarga)
            val result = extractResult(xmlString, "getCargaAcademicaByAlumnoResult")
            if (result != null) {
                json.decodeFromString<List<MateriaCarga>>(result)
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCalifFinal(matricula: String, modEducativo: Int = 1): List<MateriaFinal> {
        return try {
            val soapBody = bodyCalifFinal.replace("%d", modEducativo.toString())
            val xmlString = apiService.finales(soapBody)
            val result = extractResult(xmlString, "getAllCalifFinalByAlumnosResult")
            if (result != null) {
                json.decodeFromString<List<MateriaFinal>>(result)
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCalifUnidades(matricula: String): List<MateriaParcial> {
        return try {
            val xmlString = apiService.parciales(bodyCalifUnidades)
            val result = extractResult(xmlString, "getCalifUnidadesByAlumnoResult")
            if (result != null) {
                val rawList = json.decodeFromString<List<MateriaParcialRaw>>(result)
                rawList.map { it.toMateriaParcial() }
            } else emptyList()
        } catch (e: Exception) {
            try {
                val xmlString = apiService.parciales(bodyCalifUnidades)
                val result = extractResult(xmlString, "getCalifUnidadesByAlumnoResult")
                if (result != null) {
                    json.decodeFromString<List<MateriaParcial>>(result)
                } else emptyList()
            } catch (e2: Exception) {
                e2.printStackTrace()
                emptyList()
            }
        }
    }

    fun getMatricula(): String = userMatricula

    private fun clean(s: String): String = s
        .replace("?", "Í")
        .replace("í?", "í")
        .replace("A?", "Á")
        .replace("O?", "Ó")
        .replace("&nbsp;", " ")
        .trim()
}
