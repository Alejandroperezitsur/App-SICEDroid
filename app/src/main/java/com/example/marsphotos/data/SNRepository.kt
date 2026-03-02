package com.example.marsphotos.data

import android.util.Log
import com.example.marsphotos.model.*
import com.example.marsphotos.network.SICENETWService
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.simpleframework.xml.core.Persister

interface SNRepository {
    suspend fun acceso(matricula: String, contrasenia: String): Boolean
    suspend fun accesoObjeto(matricula: String, contrasenia: String): Usuario
    suspend fun profile(matricula: String): ProfileStudent
    suspend fun getKardex(matricula: String, lineamiento: Int = 1): List<MateriaKardex>
    suspend fun getCarga(matricula: String): List<MateriaCarga>
    suspend fun getCalifFinal(matricula: String, modEducativo: Int = 1): List<MateriaFinal>
    suspend fun getCalifUnidades(matricula: String): List<MateriaParcial>
    suspend fun getMatricula(): String
}

class NetworkSNRepository(
    private val snApiService: SICENETWService
) : SNRepository {

    private var userMatricula: String = ""

    // SOAP Body templates
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

    /**
     * Autentica al usuario en el SICEnet
     */
    override suspend fun acceso(matricula: String, contrasenia: String): Boolean {
        Log.d("SNRepository", "===== INICIANDO AUTENTICACIÓN =====")
        Log.d("SNRepository", "Matrícula: $matricula")
        
        try {
            val soapBody = bodyLogin.format(matricula, contrasenia)
            Log.d("SNRepository", "Enviando SOAP Body (truncado): ${soapBody.take(100)}...")
            
            val response = snApiService.acceso(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            Log.d("SNRepository", "Respuesta XML recibida: $xmlString")

            val resultJson = extractResult(xmlString, "accesoLoginResult")
            Log.d("SNRepository", "JSON extraído: $resultJson")

            if (resultJson != null) {
                try {
                    val jsonElement = Json { ignoreUnknownKeys = true }.parseToJsonElement(resultJson)
                    val jsonObject = jsonElement.jsonObject
                    
                    val accesoValue = jsonObject["acceso"]?.jsonPrimitive?.content ?: ""
                    val mensajeValue = jsonObject["estatus"]?.jsonPrimitive?.content
                    
                    Log.d("SNRepository", "Valor de 'acceso': $accesoValue")

                    if (accesoValue.isBlank()) {
                        Log.e("SNRepository", "Campo 'acceso' ausente en respuesta de autenticación")
                        throw IllegalStateException("Respuesta de autenticación inválida")
                    }

                    if (accesoValue.lowercase() == "true" || accesoValue == "1") {
                        userMatricula = matricula
                        return true
                    }

                    if (accesoValue.lowercase() == "false" || accesoValue == "0") {
                        val msg = mensajeValue ?: "Credenciales inválidas"
                        Log.d("SNRepository", "Autenticación rechazada: $msg")
                        return false
                    }
                } catch (e: Exception) {
                    Log.e("SNRepository", "Error parseando JSON interno", e)
                    throw IllegalStateException("Error parseando respuesta de autenticación", e)
                }
            }
            
            throw IllegalStateException("Respuesta de autenticación sin JSON válido")
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
                                semestre = semestre.toIntOrNull() ?: 0,
                                promedio = promedio,
                                estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
                                statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
                                cdtsReunidos = cdtAc.toIntOrNull() ?: 0,
                                cdtsActuales = cdtAct.toIntOrNull() ?: 0,
                                semActual = semestre.toIntOrNull() ?: 0,
                                inscrito = inscritoStr,
                                reinscripcionFecha = fReins,
                                estatusAlumno = estatusAlu,
                                estatusAcademico = estatusAcad,
                                fotoUrl = fotoUrl,
                                sinAdeudos = clean(sinAdeudos),
                                lineamiento = lin,
                                modEducativo = mod,
                                operaciones = ensureDefaultOperaciones(operaciones)
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
                                    semestre = semestre.toIntOrNull() ?: 0,
                                    promedio = promedio,
                                    estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
                                    statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
                                    cdtsReunidos = cdtAc.toIntOrNull() ?: 0,
                                    cdtsActuales = cdtAct.toIntOrNull() ?: 0,
                                    semActual = semestre.toIntOrNull() ?: 0,
                                    inscrito = inscritoStr,
                                    reinscripcionFecha = fReins,
                                    estatusAlumno = estatusAlu,
                                    estatusAcademico = estatusAcad,
                                    fotoUrl = fotoUrl,
                                    sinAdeudos = clean(sinAdeudos),
                                    lineamiento = lin,
                                    modEducativo = mod,
                                    operaciones = ensureDefaultOperaciones(operaciones)
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

        // Re-calculate ProfileStudent if it wasn't returned yet
        return ProfileStudent(
            matricula = matricula,
            nombre = clean(nombre),
            carrera = clean(carrera),
            especialidad = clean(especialidad),
            semestre = semestre.toIntOrNull() ?: 0,
            promedio = promedio,
            estado = if (estadoScraped.isEmpty()) "INSCRITO" else clean(estadoScraped),
            statusMatricula = if (sinAdeudos.isNotEmpty()) clean(sinAdeudos) else "SIN ADEUDOS",
            cdtsReunidos = cdtAc.toIntOrNull() ?: 0,
            cdtsActuales = cdtAct.toIntOrNull() ?: 0,
            semActual = semestre.toIntOrNull() ?: 0,
            inscrito = inscritoStr,
            reinscripcionFecha = fReins,
            estatusAlumno = estatusAlu,
            estatusAcademico = estatusAcad,
            fotoUrl = fotoUrl,
            lineamiento = 1,
            modEducativo = 1,
            sinAdeudos = clean(sinAdeudos),
            operaciones = ensureDefaultOperaciones(operaciones)
        )
    }

    private fun clean(s: String): String = s
        .replace("?", "Í")
        .replace("í?", "í")
        .replace("A?", "Á")
        .replace("O?", "Ó")
        .replace("&nbsp;", " ")
        .trim()

    private fun ensureDefaultOperaciones(operaciones: MutableList<String>): List<String> {
        if (operaciones.isEmpty()) {
            operaciones.add("KARDEX - Consultar Kardex")
            operaciones.add("CARGA ACADEMICA - Consultar carga académica")
            operaciones.add("CALIFICACIONES - Consultar calificaciones")
        }
        return operaciones.distinct()
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
        val startTag = "<$tag>"
        val endTag = "</$tag>"
        val start = xml.indexOf(startTag)
        val end = xml.indexOf(endTag)
        if (start != -1 && end != -1) {
            return xml.substring(start + startTag.length, end)
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
            if (result != null) {
                Log.d("SNRepository", "Kardex JSON Raw: ${result.take(500)}...")
                // El SICEnet devuelve un objeto con "lstKardex" y "Promedio"
                val json = Json { ignoreUnknownKeys = true }
                val responseObj = json.decodeFromString<KardexResponse>(result)
                return responseObj.lstKardex
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error parsing Kardex JSON (Ask Gemini)", e)
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
            if (result != null) {
                Log.d("SNRepository", "Carga JSON Raw: $result")
                return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaCarga>>(result)
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error parsing Carga JSON", e)
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
            if (result != null) {
                Log.d("SNRepository", "CalifFinal JSON Raw: $result")
                return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaFinal>>(result)
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error parsing CalifFinal JSON (Ask Gemini)", e)
        }
        return emptyList()
    }

    override suspend fun getCalifUnidades(matricula: String): List<MateriaParcial> {
        try {
            Log.d("SNRepository", "Solicitando Parciales SOAP...")
            val soapBody = bodyCalifUnidades
            val response = snApiService.parcialesSoap(soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType()))
            val xmlString = response.string()
            
            val result = extractResult(xmlString, "getCalifUnidadesByAlumnoResult")
            if (result != null) {
                Log.d("SNRepository", "Parciales JSON Raw: ${result.take(500)}...")
                return Json { ignoreUnknownKeys = true }.decodeFromString<List<MateriaParcial>>(result)
            }
        } catch (e: Exception) {
            Log.e("SNRepository", "Error parsing Parciales JSON", e)
        }
        return emptyList()
    }
}
