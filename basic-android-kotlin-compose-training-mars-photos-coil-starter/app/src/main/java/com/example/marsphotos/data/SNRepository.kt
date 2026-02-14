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
import com.example.marsphotos.network.bodyperfil
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
    suspend fun getKardex(matricula: String): List<MateriaKardex>

    /** Obtiene la Carga Académica */
    suspend fun getCarga(matricula: String): List<MateriaCarga>

    /** Obtiene Calificaciones Parciales */
    suspend fun getCalifUnidades(matricula: String): List<MateriaParcial>

    /** Obtiene Calificaciones Finales */
    suspend fun getCalifFinal(matricula: String): List<MateriaFinal>

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

    override suspend fun getKardex(matricula: String): List<MateriaKardex> = emptyList()
    override suspend fun getCarga(matricula: String): List<MateriaCarga> = emptyList()
    override suspend fun getCalifUnidades(matricula: String): List<MateriaParcial> = emptyList()
    override suspend fun getCalifFinal(matricula: String): List<MateriaFinal> = emptyList()

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
        Log.e("SNRepository", "===== INICIANDO OBTENCIÓN DE PERFIL =====")
        
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
        var kardexList = mutableListOf<com.example.marsphotos.model.MateriaKardex>()
        var cargaList = mutableListOf<com.example.marsphotos.model.MateriaCarga>()
        var parcialesList = mutableListOf<com.example.marsphotos.model.MateriaParcial>()
        var kTitle = ""
        var cTitle = ""
        var pTitle = ""
        var kHtmlStr = ""
        var cHtmlStr = ""
        var pHtmlStr = ""
        var estadoScraped = ""
        var statusMatriculaScraped = ""

        // 1. Obtener datos via SOAP (getAlumnoAcademico)
        try {
            val soapBody = bodyperfil
            Log.e("SNRepository", ">>> Pidiendo Perfil SOAP (getAlumnoAcademico) <<<")
            
            val response = try {
                snApiService.perfil(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            } catch (e: retrofit2.HttpException) {
                Log.e("SNRepository", "❌ PERFIL SOAP ERROR ${e.code()}")
                null
            }

            if (response != null) {
                val xmlString = response.string()
                // Intento 1: Regex para extraer el JSON o XML del resultado
                var resultText = Regex("<getAlumnoAcademicoResult>(.*?)</getAlumnoAcademicoResult>").find(xmlString)?.groupValues?.get(1)
                
                if (resultText == null) {
                    try {
                        val envelope = Persister().read(com.example.marsphotos.model.EnvelopeSobreAlumno::class.java, xmlString)
                        resultText = envelope.body?.getAlumnoAcademicoResponse?.getAlumnoAcademicoResult
                    } catch (e: Exception) {}
                }
                
                if (!resultText.isNullOrBlank()) {
                    var processed: String = resultText
                    // Desencapsular si es XML
                    while (processed.contains("&lt;")) {
                        processed = processed.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
                    }

                    // CASO JSON (Detectado en logs)
                    if (processed.trim().startsWith("{")) {
                        Log.e("SNRepository", ">>> Parseando JSON de Perfil <<<")
                        try {
                            val json = Json.parseToJsonElement(processed.trim()).jsonObject
                            nombre = json["nombre"]?.jsonPrimitive?.content ?: ""
                            carrera = json["carrera"]?.jsonPrimitive?.content ?: ""
                            especialidad = json["especialidad"]?.jsonPrimitive?.content ?: ""
                            semestre = json["semActual"]?.jsonPrimitive?.content ?: ""
                            cdtAc = json["cdtosAcumulados"]?.jsonPrimitive?.content ?: "0"
                            cdtAct = json["cdtosActuales"]?.jsonPrimitive?.content ?: "0"
                            inscritoStr = if (json["inscrito"]?.jsonPrimitive?.content == "true") "SI" else "NO"
                            fReins = json["fechaReins"]?.jsonPrimitive?.content ?: ""
                            estatusAlu = json["estatus"]?.jsonPrimitive?.content ?: ""
                            val foto = json["urlFoto"]?.jsonPrimitive?.content ?: ""
                            if (foto.isNotEmpty()) fotoUrl = "https://sicenet.itsur.edu.mx/fotos/$foto"
                            
                            Log.e("SNRepository", "✅ Datos JSON extraídos: $nombre")
                        } catch (e: Exception) {
                            Log.e("SNRepository", "❌ Error en JSON: ${e.message}")
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
                                Log.e("SNRepository", "✅ Datos XML extraídos: $nombre, $carrera")
                            }
                        } catch (e: Exception) {
                            Log.e("SNRepository", "❌ Error parseando XML de Perfil: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "❌ Error en SOAP: ${e.message}")
        }
        var kardexUrl = "/frmKardex.aspx"
        var cargaUrl = "/frmCargaAcademica.aspx"
        var califUrl = "/frmCalificaciones.aspx"

        try {
            Log.e("SNRepository", ">>> STEP 1: Scraping Plataforma (Session Keep-Alive) <<<")
            val pResponse = snApiService.plataforma()
            val html = pResponse.string()
            val doc = Jsoup.parse(html)
            
            // Perfil básico del HTML (Usando IDs del HTML del usuario)
            if (nombre.isEmpty()) nombre = doc.selectFirst("#spnNombre")?.text()?.trim() ?: doc.selectFirst("#lblNombre")?.text()?.trim() ?: ""
            if (fotoUrl.isEmpty()) fotoUrl = doc.selectFirst("#imgFoto, #imgAlumno")?.absUrl("src") ?: ""
            if (carrera.isEmpty()) carrera = doc.selectFirst("#spnCarrera")?.text()?.trim() ?: ""
            if (especialidad.isEmpty()) especialidad = doc.selectFirst("#spnEspecilidad, td:contains(Especialidad) + td")?.text()?.trim() ?: ""
            if (semestre.isEmpty()) semestre = doc.selectFirst("#spnSemActual, td:contains(Sem. Actual) + td")?.text()?.trim() ?: ""
            
            estadoScraped = doc.selectFirst("#spnEstatus, td:contains(Estado) + td")?.text()?.trim() ?: ""
            statusMatriculaScraped = doc.selectFirst("td:contains(Status Matrícula) + td, td:contains(Estatus Matrícula) + td")?.text()?.trim() ?: ""
            
            val estatusAcadScraped = doc.selectFirst("td:contains(Estatus Académico) + td")?.text()?.trim() ?: ""
            val estatusAluScraped = doc.selectFirst("td:contains(Estatus Alumno) + td")?.text()?.trim() ?: ""

            if (estatusAcad.isEmpty()) estatusAcad = estatusAcadScraped
            if (estatusAlu.isEmpty()) estatusAlu = estatusAluScraped

            if (cdtAc == "0") cdtAc = doc.selectFirst("#spnCdtosAcumulados")?.text()?.trim() ?: "0"
            if (cdtAct == "0") cdtAct = doc.selectFirst("#spnCdtosActuales")?.text()?.trim() ?: "0"
            if (inscritoStr == "NO") inscritoStr = doc.selectFirst("#spnInscrito")?.text()?.trim() ?: "NO"
            if (fReins.isEmpty()) fReins = doc.selectFirst("#spnFechaReins")?.text()?.trim() ?: ""
            if (sinAdeudos.isEmpty()) sinAdeudos = doc.selectFirst("#pAdeudo")?.text()?.trim() ?: ""
            
            if (sinAdeudos.isEmpty()) sinAdeudos = doc.select("td, span").find { it.text().contains("ADEUDOS") }?.text()?.trim() ?: ""
            
            doc.select("a").forEach { a ->
                val txt = a.text().uppercase()
                var href = a.attr("href")
                if (txt.contains("CALIFICACIONES") || txt.contains("KARDEX") || 
                    txt.contains("MONITOREO") || txt.contains("REINSCRIPCION") || 
                    txt.contains("CARGA") || txt.contains("CERRAR SESION")) {
                    
                    operaciones.add(txt)
                    
                    // Si el href es #, usamos rutas estimadas basadas en el JS del sitio
                    if (href == "#" || href.isBlank()) {
                        href = when {
                            txt.contains("KARDEX") -> "/frmConsultaKardex2015.aspx"
                            txt.contains("CARGA") -> "/frmCargaAcademica2015.aspx"
                            txt.contains("CALIFICACIONES") -> "/frmConsultaCalificaciones.aspx"
                            else -> href
                        }
                    }

                    if (txt.contains("KARDEX")) kardexUrl = href
                    if (txt.contains("CARGA")) cargaUrl = href
                    if (txt.contains("CALIFICACIONES")) califUrl = href
                }
            }
            Log.e("SNRepository", "✅ Step 1 OK. URLs: K:$kardexUrl, C:$cargaUrl, P:$califUrl")

            // --- STEP 2: KARDEX ---
            try {
                if (kardexUrl.contains("javascript:")) {
                    kHtmlStr = "URL no válida (JavaScript)"
                } else {
                    Log.e("SNRepository", ">>> STEP 2: Fetching KARDEX ($kardexUrl) <<<")
                    val kHtml = snApiService.dynamicGet(kardexUrl).string()
                    kHtmlStr = if (kHtml.isEmpty()) "Vacio" else if (kHtml.length > 1000) kHtml.take(1000) else kHtml
                    val kDoc = Jsoup.parse(kHtml)
                    kTitle = "KARDEX: " + kDoc.title()
                    
                    promedio = kDoc.selectFirst("td:contains(Promedio general) + td, #lblPromedioGeneral, .promedio")?.text()?.trim() ?: ""
                    
                    kDoc.select("tr").forEach { tr ->
                        val tds = tr.select("td")
                        if (tds.size >= 5) {
                            val rowTxt = tr.text().uppercase()
                            if (rowTxt.contains("APROBADA") || rowTxt.contains("REPROBADA") || rowTxt.contains("CURSANDO") || (tds[0].text().trim().firstOrNull()?.isDigit() == true)) {
                                kardexList.add(com.example.marsphotos.model.MateriaKardex(
                                    clave = tds.getOrNull(1)?.text()?.trim() ?: "",
                                    nombre = tds.getOrNull(2)?.text()?.trim() ?: "",
                                    calificacion = if (tds.size > 5) tds[5].text().trim() else "",
                                    acreditacion = if (tds.size > 6) tds[6].text().trim() else "",
                                    periodo = if (tds.size > 8) (tds[7].text().trim() + " " + tds[8].text().trim()) else ""
                                ))
                            }
                        }
                    }
                    Log.e("SNRepository", "✅ Step 2 OK. Kardex items: ${kardexList.size}")
                }
            } catch (e: Exception) { 
                kHtmlStr = "ERROR PASO 2: ${e.message}"
            }

            // --- STEP 3: CARGA ---
            try {
                if (cargaUrl.contains("javascript:")) {
                    cHtmlStr = "URL no válida (JavaScript)"
                } else {
                    Log.e("SNRepository", ">>> STEP 3: Fetching CARGA ($cargaUrl) <<<")
                    val cHtml = snApiService.dynamicGet(cargaUrl).string()
                    cHtmlStr = if (cHtml.isEmpty()) "Vacio" else if (cHtml.length > 1000) cHtml.take(1000) else cHtml
                    val cDoc = Jsoup.parse(cHtml)
                    cTitle = "CARGA: " + cDoc.title()

                    cDoc.select("tr").forEach { tr ->
                        val tds = tr.select("td")
                        if (tds.size >= 6) { 
                            val txt = tr.text().uppercase()
                            if (txt.contains("AULA") || txt.contains("DOCENTE") || tds.any { it.text().trim() == "O" }) {
                                 val possibleNombre = tds.getOrNull(1)?.text()?.split("\n")?.firstOrNull()?.trim() ?: ""
                                 if (possibleNombre.length > 3) {
                                     cargaList.add(com.example.marsphotos.model.MateriaCarga(
                                        nombre = possibleNombre,
                                        docente = tds.getOrNull(1)?.text()?.split("\n")?.getOrNull(1)?.trim() ?: "",
                                        grupo = tds.getOrNull(3)?.text()?.trim() ?: "",
                                        creditos = tds.getOrNull(5)?.text()?.trim() ?: "",
                                        lunes = tds.getOrNull(6)?.text()?.trim() ?: "",
                                        martes = tds.getOrNull(7)?.text()?.trim() ?: "",
                                        miercoles = tds.getOrNull(8)?.text()?.trim() ?: "",
                                        jueves = tds.getOrNull(9)?.text()?.trim() ?: "",
                                        viernes = tds.getOrNull(10)?.text()?.trim() ?: ""
                                    ))
                                 }
                            }
                        }
                    }
                    Log.e("SNRepository", "✅ Step 3 OK. Carga items: ${cargaList.size}")
                }
            } catch (e: Exception) { 
                cHtmlStr = "ERROR PASO 3: ${e.message}"
            }

            // --- STEP 4: CALIFICACIONES ---
            try {
                if (califUrl.contains("javascript:")) {
                    pHtmlStr = "URL no válida (JavaScript)"
                } else {
                    Log.e("SNRepository", ">>> STEP 4: Fetching CALIFICACIONES ($califUrl) <<<")
                    val pHtml = snApiService.dynamicGet(califUrl).string()
                    pHtmlStr = if (pHtml.isEmpty()) "Vacio" else if (pHtml.length > 1000) pHtml.take(1000) else pHtml
                    val pDoc = Jsoup.parse(pHtml)
                    pTitle = "CALIF: " + pDoc.title()

                    pDoc.select("tr").forEach { tr ->
                        val tds = tr.select("td")
                        if (tds.size >= 3) { 
                             val matName = tds.getOrNull(1)?.text()?.trim() ?: ""
                             if (matName.length > 5 && (tr.text().any { it.isDigit() })) {
                                val pars = mutableListOf<String>()
                                for (i in 2 until tds.size) {
                                    val score = tds[i].text().trim()
                                    if (score.length <= 3) pars.add(score)
                                    if (pars.size >= 8) break
                                }
                                parcialesList.add(com.example.marsphotos.model.MateriaParcial(
                                    materia = matName,
                                    parciales = pars
                                ))
                             }
                        }
                    }
                    Log.e("SNRepository", "✅ Step 4 OK. Calif items: ${parcialesList.size}")
                }
            } catch (e: Exception) { 
                pHtmlStr = "ERROR PASO 4: ${e.message}"
            }

            // --- OPTIONAL STEP 5: SOAP FALLBACK ---
            // If scraping failed (lists are empty), try to fetch data using SOAP methods
            if (kardexList.isEmpty()) {
                Log.e("SNRepository", ">>> Fallback: Fetching Kardex via SOAP <<<")
                try {
                    val kData = getKardex(matricula)
                    if (kData.isNotEmpty()) {
                        kardexList.addAll(kData)
                        Log.e("SNRepository", "✅ Fallback Kardex OK: ${kardexList.size}")
                    }
                } catch (e: Exception) {
                    Log.e("SNRepository", "Fallback Kardex Failed: ${e.message}")
                }
            }

            if (cargaList.isEmpty()) {
                Log.e("SNRepository", ">>> Fallback: Fetching Carga via SOAP <<<")
                try {
                    val cData = getCarga(matricula)
                    if (cData.isNotEmpty()) {
                        cargaList.addAll(cData)
                        Log.e("SNRepository", "✅ Fallback Carga OK: ${cargaList.size}")
                    }
                } catch (e: Exception) {
                    Log.e("SNRepository", "Fallback Carga Failed: ${e.message}")
                }
            }

            if (parcialesList.isEmpty()) {
                Log.e("SNRepository", ">>> Fallback: Fetching Parciales via SOAP <<<")
                try {
                    val pData = getCalifUnidades(matricula)
                    if (pData.isNotEmpty()) {
                        parcialesList.addAll(pData)
                        Log.e("SNRepository", "✅ Fallback Parciales OK: ${parcialesList.size}")
                    }
                } catch (e: Exception) {
                    Log.e("SNRepository", "Fallback Parciales Failed: ${e.message}")
                }
            }


        } catch (e: Exception) {
            Log.e("SNRepository", "⚠️ Error Crítico en Scraping Secuencial: ${e.message}")
        }

        // 3. Mapeo y Limpieza
        fun mapStatus(s: String): String = when(s.uppercase().trim()) {
            "VI" -> "VIGENTE"
            "BA" -> "BAJA"
            "EG" -> "EGRESADO"
            "TI" -> "TITULADO"
            "IN" -> "INSCRITO"
            else -> s
        }

        fun clean(s: String): String = s
            .replace("?", "Í")
            .replace("í?", "í")
            .replace("A?", "Á")
            .replace("O?", "Ó")
            .replace("&nbsp;", " ")
            .trim()

        return ProfileStudent(
            matricula = matricula,
            nombre = clean(nombre),
            carrera = clean(carrera),
            especialidad = clean(especialidad),
            semestre = semestre,
            promedio = promedio,
            estado = if (estadoScraped.isEmpty() || estadoScraped.contains("INSCRITO", true)) "INSCRITO" else clean(estadoScraped),
            statusMatricula = if (statusMatriculaScraped.isEmpty() || statusMatriculaScraped.contains("ADEUDOS", true)) {
                if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS"
            } else clean(statusMatriculaScraped),
            cdtsReunidos = cdtAc,
            cdtsActuales = cdtAct,
            inscrito = inscritoStr,
            reinscripcionFecha = fReins,
            estatusAlumno = mapStatus(estatusAlu),
            estatusAcademico = clean(estatusAcad),
            fotoUrl = fotoUrl,
            sinAdeudos = clean(sinAdeudos),
            operaciones = operaciones.distinct(),
            kardex = kardexList,
            cargaAcademica = cargaList,
            calificacionesParciales = parcialesList,
            kardexTitle = kTitle,
            cargaTitle = cTitle,
            califTitle = pTitle,
            kardexHtml = kHtmlStr,
            cargaHtml = cHtmlStr,
            califHtml = pHtmlStr
        )
    }

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

    override suspend fun getKardex(matricula: String): List<MateriaKardex> {
        try {
            Log.d("SNRepository", "Solicitando Kardex SOAP...")
            val soapBody = bodyKardex.format(1)
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

    override suspend fun getCalifFinal(matricula: String): List<MateriaFinal> {
        try {
            Log.d("SNRepository", "Solicitando CalifFinal SOAP...")
            val soapBody = bodyCalifFinal
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
