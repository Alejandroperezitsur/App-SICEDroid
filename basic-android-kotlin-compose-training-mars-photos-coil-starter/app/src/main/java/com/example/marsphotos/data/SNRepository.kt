/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.data

import android.util.Log
import com.example.marsphotos.model.AccesoLoginResponse
import com.example.marsphotos.model.AlumnoInfo
import com.example.marsphotos.model.BodyAccesoResponse
import com.example.marsphotos.model.EnvelopeSobreAcceso
import com.example.marsphotos.model.PerfilDataSet
import com.example.marsphotos.model.MateriaKardex
import com.example.marsphotos.model.MateriaCarga
import com.example.marsphotos.model.MateriaParcial
import com.example.marsphotos.model.MateriaFinal
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.model.Usuario
import com.example.marsphotos.network.SICENETWService
import com.example.marsphotos.network.bodyacceso
import com.example.marsphotos.network.bodyPerfilWithLineamiento
import com.example.marsphotos.network.bodyKardex
import com.example.marsphotos.network.bodyCarga
import com.example.marsphotos.network.bodyParciales
import com.example.marsphotos.network.bodyCalifFinal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.simpleframework.xml.core.Persister
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.net.URL

/**
 * Interface para acceder a los servicios SICENET
 */
interface SNRepository {
    /** Autenticación en SICENET */
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    
    /** Obtiene el objeto Usuario autenticado */
    suspend fun accesoObjeto(matricula: String, contrasenia: String): Usuario
    
    /** Obtiene el perfil académico del estudiante */
    suspend fun profile(matricula: String): ProfileStudent
    
    /** Obtiene el Kardex */
    suspend fun getKardex(matricula: String, lineamiento: Int = 1): List<MateriaKardex>

    /** Obtiene la Carga Académica */
    suspend fun getCarga(matricula: String): List<MateriaCarga>

    /** Obtiene Calificaciones Parciales */
    suspend fun getCalifUnidades(matricula: String): List<MateriaParcial>

    /** Obtiene Calificaciones Finales */
    suspend fun getCalifFinal(matricula: String, modEducativo: Int = 1): List<MateriaFinal>

    /** Obtiene la matrícula del usuario autenticado */
    suspend fun getMatricula(): String
}

/**
 * Implementación local usando base de datos
 */
class DBLocalSNRepository(val apiDB: Any) : SNRepository {
    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        return false
    }

    override suspend fun accesoObjeto(matricula: String, contrasenia: String): Usuario {
        return Usuario(matricula = "")
    }

    override suspend fun profile(matricula: String): ProfileStudent {
        return ProfileStudent()
    }

    override suspend fun getKardex(matricula: String, lineamiento: Int): List<MateriaKardex> = emptyList()
    override suspend fun getCarga(matricula: String): List<MateriaCarga> = emptyList()
    override suspend fun getCalifUnidades(matricula: String): List<MateriaParcial> = emptyList()
    override suspend fun getCalifFinal(matricula: String, modEducativo: Int): List<MateriaFinal> = emptyList()

    override suspend fun getMatricula(): String {
        return ""
    }
}

/**
 * Implementación de red que conecta con el servicio SICENET SOAP
 */
class NetworkSNRepository(
    private val snApiService: SICENETWService
) : SNRepository {
    
    private var userMatricula: String = ""

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Realiza la autenticación en SICENET
     */
    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        Log.d("SNRepository", "===== INICIANDO AUTENTICACIÓN =====")
        Log.d("SNRepository", "Matrícula: $matricula")
        
        return try {
            val safeMatricula = escapeXml(matricula)
            val safeContrasenia = escapeXml(contrasenia)
            val soapBody = bodyacceso.format(safeMatricula.uppercase(), safeContrasenia)
            
            Log.d("SNRepository", "Enviando SOAP Body (truncado): ${soapBody.take(100)}...")
            
            // Usamos text/xml; charset=utf-8 explícitamente
            val response = try {
                snApiService.acceso(soapBody.toRequestBody("text/xml;charset=utf-8".toMediaType()))
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SNRepository", "❌ HTTP Error ${e.code()}: $errorBody")
                throw e
            }
            
            val xmlString = response.string()
            Log.d("SNRepository", "Respuesta XML recibida: $xmlString")
            
            // Verificación robusta de éxito
            if (xmlString.contains("true", ignoreCase = true) || xmlString.contains(">1<")) {
                userMatricula = matricula
                return true
            }
            val startIdx = xmlString.indexOf('{')
            val endIdx = xmlString.lastIndexOf('}')
            
            if (startIdx != -1 && endIdx != -1) {
                val jsonString = xmlString.substring(startIdx, endIdx + 1).trim()
                Log.d("SNRepository", "JSON extraído: $jsonString")
                
                try {
                    val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
                    val accesoValue = jsonObject["acceso"]?.jsonPrimitive?.content
                    
                    Log.d("SNRepository", "Valor de 'acceso': $accesoValue")
                    
                    if (accesoValue?.lowercase() == "true" || accesoValue == "1") {
                        userMatricula = matricula
                        return true
                    }
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parseando JSON interno", e)
                }
            } else {
                // Si no es JSON, intentar parsear XML estándar usando SimpleXML
                // Aquí podríamos usar el persister si fuera necesario, pero por ahora
                // verificamos si contiene indicadores de éxito simples
                if (xmlString.contains("true") || xmlString.contains(">true<")) {
                    userMatricula = matricula
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.e("SNRepository", "❌ Error en autenticación: ${e.message}", e)
            throw e
        }
    }

    /**
     * Obtiene el usuario autenticado como objeto
     */
    override suspend fun accesoObjeto(matricula: String, contrasenia: String): Usuario {
        return if (acceso(matricula, contrasenia)) {
            Usuario(matricula = matricula)
        } else {
            Usuario(matricula = "")
        }
    }

    /**
     * Obtiene el perfil académico del estudiante
     */
    override suspend fun profile(matricula: String): ProfileStudent {
        Log.e("SNRepository", "===== OBTENIENDO PERFIL OPTIMIZADO =====")
        
        var nombre = ""
        var carrera = ""
        var especialidad = ""
        var semestre = ""
        var promedio = ""
        var cdtAc = "0"
        var cdtAct = "0"
        var inscritoStr = "NO"
        var fReins = ""
        var estatusAlu = ""
        var estatusAcad = ""
        var fotoUrl = ""
        var sinAdeudos = ""
        var operaciones = mutableListOf<String>()
        var estadoScraped = ""
        var statusMatriculaScraped = ""

        // 1. Obtener datos via SOAP (getAlumnoAcademicoWithLineamiento)
        try {
            val soapBody = bodyPerfilWithLineamiento
            Log.e("SNRepository", ">>> Pidiendo Perfil SOAP (getAlumnoAcademicoWithLineamiento) <<<")
            
            val response = try {
                snApiService.perfil(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: retrofit2.HttpException) {
                Log.e("SNRepository", "❌ PERFIL SOAP ERROR ${e.code()}")
                null
            }

            if (response != null) {
                val xmlString = response.string()
                var resultText = extractResult(xmlString, "getAlumnoAcademicoWithLineamientoResult")
                
                if (resultText == null) {
                    resultText = Regex("<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>").find(xmlString)?.groupValues?.get(1)
                }
                
                if (!resultText.isNullOrBlank()) {
                    var processed: String = resultText
                    // Desencapsular si es XML
                    while (processed.contains("&lt;")) {
                        processed = processed.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
                    }

                    Log.d("SNRepository", "Processed Profile Result: $processed")

                    // CASO JSON (SICEnet suele retornar JSON string dentro del XML)
                    if (processed.trim().startsWith("{")) {
                        try {
                            val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(processed.trim()).jsonObject
                            nombre = json["nombre"]?.jsonPrimitive?.content ?: ""
                            carrera = json["carrera"]?.jsonPrimitive?.content ?: ""
                            especialidad = json["especialidad"]?.jsonPrimitive?.content ?: ""
                            semestre = json["semActual"]?.jsonPrimitive?.content ?: ""
                            cdtAc = json["cdtosAcumulados"]?.jsonPrimitive?.content ?: "0"
                            cdtAct = json["cdtosActuales"]?.jsonPrimitive?.content ?: "0"
                            inscritoStr = if (json["inscrito"]?.jsonPrimitive?.content == "true" || json["inscrito"]?.jsonPrimitive?.content == "SI") "SI" else "NO"
                            fReins = json["fechaReins"]?.jsonPrimitive?.content ?: ""
                            estatusAlu = json["estatus"]?.jsonPrimitive?.content ?: ""
                            val foto = json["urlFoto"]?.jsonPrimitive?.content ?: ""
                            if (foto.isNotEmpty()) fotoUrl = "https://sicenet.itsur.edu.mx/fotos/$foto"
                            
                            val lin = json["lineamiento"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                            val mod = json["modEducativo"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                            
                            Log.e("SNRepository", "✅ Datos JSON de perfil extraídos (Lin: $lin, Mod: $mod)")

                            return ProfileStudent(
                                matricula = matricula,
                                nombre = clean(nombre),
                                carrera = clean(carrera),
                                especialidad = clean(especialidad),
                                semestre = semestre,
                                promedio = promedio,
                                estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
                                statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
                                cdtsReunidos = cdtAc,
                                cdtsActuales = cdtAct,
                                inscrito = inscritoStr,
                                reinscripcionFecha = fReins,
                                estatusAlumno = estatusAlu,
                                estatusAcademico = estatusAcad,
                                fotoUrl = fotoUrl,
                                sinAdeudos = clean(sinAdeudos),
                                lineamiento = lin,
                                modEducativo = mod,
                                operaciones = operaciones.distinct()
                            )
                        } catch (e: Exception) {
                            Log.e("SNRepository", "❌ Error parsing profile JSON: ${e.message}")
                        }
                    } else if (processed.contains("<Alumno>")) {
                        // CASO XML
                        try {
                            val xmlToParse = if (processed.contains("<DataSet")) processed.substring(processed.indexOf("<DataSet")) else processed
                            val alu = Persister().read(com.example.marsphotos.model.PerfilDataSet::class.java, xmlToParse).alumno
                            if (alu != null) {
                                nombre = "${alu.nombre ?: ""} ${alu.apellidos ?: ""}".trim()
                                carrera = alu.carrera ?: ""
                                especialidad = alu.especialidad ?: ""
                                semestre = alu.semActual ?: alu.semestre ?: ""
                                promedio = alu.promedio ?: ""
                                estatusAlu = alu.estado ?: ""
                                cdtAc = alu.cdtosAcumulados ?: "0"
                                cdtAct = alu.cdtosActuales ?: "0"
                                inscritoStr = alu.inscrito ?: "NO"
                                fReins = alu.fechaReins ?: ""
                                val lin = alu.lineamiento ?: 1
                                val mod = alu.modEducativo ?: 1
                                Log.e("SNRepository", "✅ Datos XML de perfil extraídos")
                                return ProfileStudent(
                                    matricula = matricula,
                                    nombre = clean(nombre),
                                    carrera = clean(carrera),
                                    especialidad = clean(especialidad),
                                    semestre = semestre,
                                    promedio = promedio,
                                    estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
                                    statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
                                    cdtsReunidos = cdtAc,
                                    cdtsActuales = cdtAct,
                                    inscrito = inscritoStr,
                                    reinscripcionFecha = fReins,
                                    estatusAlumno = estatusAlu,
                                    estatusAcademico = estatusAcad,
                                    fotoUrl = fotoUrl,
                                    sinAdeudos = clean(sinAdeudos),
                                    lineamiento = lin,
                                    modEducativo = mod,
                                    operaciones = operaciones.distinct()
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("SNRepository", "❌ Error parseando XML de Perfil: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "❌ Error en SOAP de Perfil: ${e.message}")
        }

        // ... (Scraping fallback if SOAP failed return) ...
        // Re-calculate ProfileStudent if it wasn't returned yet
        return ProfileStudent(
            matricula = matricula,
            nombre = clean(nombre),
            carrera = clean(carrera),
            especialidad = clean(especialidad),
            semestre = semestre,
            promedio = promedio,
            estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
            statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
            cdtsReunidos = cdtAc,
            cdtsActuales = cdtAct,
            inscrito = inscritoStr,
            reinscripcionFecha = fReins,
            estatusAlumno = estatusAlu,
            estatusAcademico = estatusAcad,
            fotoUrl = fotoUrl,
            lineamiento = 1, // Fallback
            modEducativo = 1, // Fallback
            sinAdeudos = clean(sinAdeudos),
            operaciones = operaciones.distinct()
        )
    }

    private fun clean(s: String): String = s
        .replace("?", "Í")
        .replace("í?", "í")
        .replace("A?", "Á")
        .replace("O?", "Ó")
        .replace("&nbsp;", " ")
        .trim()

    /**
     * Obtiene la matrícula del usuario autenticado
     */
    override suspend fun getMatricula(): String {
        return userMatricula
    }

    private fun unescapeXml(input: String): String {
        return input.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }

    private fun extractResult(xml: String, tag: String): String? {
        try {
            // Regex to match <tag ...>content</tag> or <tag>content</tag>
            // Handles potential namespaces or attributes
            val regex = Regex("<$tag.*?>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(xml)
            if (match != null) {
                val content = match.groupValues[1]
                return unescapeXml(content)
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error extracting result for tag $tag", e)
        }
        return null
    }

    override suspend fun getKardex(matricula: String, lineamiento: Int): List<MateriaKardex> {
        try {
            Log.d("SNRepository", "Solicitando Kardex SOAP ($lineamiento)...")
            val soapBody = bodyKardex.format(lineamiento)
            val response = snApiService.kardexSoap(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            val result = extractResult(xmlString, "getAllKardexConPromedioByAlumnoResult")
            
            if (!result.isNullOrBlank()) {
                Log.d("SNRepository", "Kardex JSON Raw: $result")
                 try {
                    return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaKardex>>(result)
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parsing Kardex JSON", e)
                }
            } else {
                Log.e("SNRepository", "Kardex result is null or blank")
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error fetching Kardex", e)
        }
        return emptyList()
    }

    override suspend fun getCarga(matricula: String): List<MateriaCarga> {
        try {
            Log.d("SNRepository", "Solicitando Carga SOAP...")
            val soapBody = bodyCarga
            val response = snApiService.cargaSoap(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            val result = extractResult(xmlString, "getCargaAcademicaByAlumnoResult")
            
            if (!result.isNullOrBlank()) {
                Log.d("SNRepository", "Carga JSON Raw: $result")
                 try {
                    return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaCarga>>(result)
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parsing Carga JSON", e)
                }
            } else {
                Log.e("SNRepository", "Carga result is null or blank")
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error fetching Carga", e)
        }
        return emptyList()
    }

    override suspend fun getCalifUnidades(matricula: String): List<MateriaParcial> {
        try {
            Log.d("SNRepository", "Solicitando Parciales SOAP...")
            val soapBody = bodyParciales
            val response = snApiService.parcialesSoap(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            val result = extractResult(xmlString, "getCalifUnidadesByAlumnoResult")
            
            if (!result.isNullOrBlank()) {
                Log.d("SNRepository", "Parciales JSON Raw: $result")
                 try {
                    val jsonArray = Json.parseToJsonElement(result).jsonArray
                    val list = mutableListOf<MateriaParcial>()
                    jsonArray.forEach { element ->
                        val obj = element.jsonObject
                        val materia = obj["materia"]?.jsonPrimitive?.content ?: ""
                        val parciales = mutableListOf<String>()
                        for (i in 1..10) {
                            val p = obj["p$i"]?.jsonPrimitive?.content
                            if (!p.isNullOrEmpty()) {
                                parciales.add(p)
                            }
                        }
                        if (materia.isNotEmpty()) {
                            list.add(MateriaParcial(materia, parciales))
                        }
                    }
                    return list
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parsing Parciales JSON", e)
                }
            } else {
                Log.e("SNRepository", "Parciales result is null or blank")
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error fetching Parciales", e)
        }
        return emptyList()
    }

    override suspend fun getCalifFinal(matricula: String, modEducativo: Int): List<MateriaFinal> {
        try {
            Log.d("SNRepository", "Solicitando CalifFinal SOAP ($modEducativo)...")
            val soapBody = bodyCalifFinal.format(modEducativo)
            val response = snApiService.finalSoap(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            val result = extractResult(xmlString, "getAllCalifFinalByAlumnosResult")
            
            if (!result.isNullOrBlank()) {
                Log.d("SNRepository", "CalifFinal JSON Raw: $result")
                 try {
                    return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaFinal>>(result)
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parsing CalifFinal JSON", e)
                }
            } else {
                Log.e("SNRepository", "CalifFinal result is null or blank")
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error fetching CalifFinal", e)
        }
        return emptyList()
    }
}

// Importar MediaType para usar toMediaType()
