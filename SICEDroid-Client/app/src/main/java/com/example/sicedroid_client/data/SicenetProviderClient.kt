package com.example.sicedroid_client.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.example.sicedroid_client.model.CalifFinal
import com.example.sicedroid_client.model.CalifUnidad
import com.example.sicedroid_client.model.CargaEntry
import com.example.sicedroid_client.model.KardexEntry
import com.example.sicedroid_client.model.ProviderResult
import com.example.sicedroid_client.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Contract class que replica las definiciones del Content Provider de SICEDroid.
 * El cliente debe usar estas mismas URIs y columnas.
 */
object SicenetProviderContract {
    const val CONTENT_AUTHORITY = "com.example.marsphotos.provider"
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

    object Paths {
        const val STUDENT = "student"
        const val KARDEX = "kardex"
        const val CARGA = "carga"
        const val CALIF_UNIDAD = "califunidad"
        const val CALIF_FINAL = "califfinal"
    }

    object Student {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.STUDENT).build()
        const val MATRICULA = "matricula"
        const val NOMBRE = "nombre"
        const val APELLIDOS = "apellidos"
        const val CARRERA = "carrera"
        const val SEMESTRE = "semestre"
        const val PROMEDIO = "promedio"
        const val ESTADO = "estado"
        const val FOTO_URL = "fotoUrl"
        const val ESPECIALIDAD = "especialidad"
        const val CDTS_REUNIDOS = "cdtsReunidos"
        const val CDTS_ACTUALES = "cdtsActuales"
        const val SEM_ACTUAL = "semActual"
        const val INSCRITO = "inscrito"
        const val ESTATUS_ACADEMICO = "estatusAcademico"
        const val ESTATUS_ALUMNO = "estatusAlumno"
        const val LAST_UPDATE = "lastUpdate"
    }

    object Kardex {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.KARDEX).build()
        const val MATRICULA = "matricula"
        const val CLAVE = "clave"
        const val NOMBRE = "nombre"
        const val CALIFICACION = "calificacion"
        const val ACREDITACION = "acreditacion"
        const val PERIODO = "periodo"
        const val LAST_UPDATE = "lastUpdate"
    }

    object Carga {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CARGA).build()
        const val MATRICULA = "matricula"
        const val NOMBRE = "nombre"
        const val DOCENTE = "docente"
        const val GRUPO = "grupo"
        const val CREDITOS = "creditos"
        const val LUNES = "lunes"
        const val MARTES = "martes"
        const val MIERCOLES = "miercoles"
        const val JUEVES = "jueves"
        const val VIERNES = "viernes"
        const val SABADO = "sabado"
        const val LAST_UPDATE = "lastUpdate"
    }

    object CalifUnidad {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CALIF_UNIDAD).build()
        const val MATRICULA = "matricula"
        const val MATERIA = "materia"
        const val PARCIALES = "parciales"
        const val LAST_UPDATE = "lastUpdate"
    }

    object CalifFinal {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(Paths.CALIF_FINAL).build()
        const val MATRICULA = "matricula"
        const val MATERIA = "materia"
        const val CALIF = "calif"
        const val LAST_UPDATE = "lastUpdate"
    }

    // Permisos
    const val PERMISSION_READ = "com.example.marsphotos.provider.READ"
    const val PERMISSION_WRITE = "com.example.marsphotos.provider.WRITE"
}

/**
 * Cliente del Content Provider de SICEDroid.
 * Proporciona métodos para consultar y modificar datos académicos.
 */
class SicenetProviderClient(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val TAG = "SicenetProviderClient"

    /**
     * Verifica si la app tiene el permiso de lectura concedido.
     */
    fun hasReadPermission(): Boolean {
        return context.checkSelfPermission(SicenetProviderContract.PERMISSION_READ) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si la app tiene el permiso de escritura concedido.
     */
    fun hasWritePermission(): Boolean {
        return context.checkSelfPermission(SicenetProviderContract.PERMISSION_WRITE) ==
                PackageManager.PERMISSION_GRANTED
    }

    // ==================== OPERACIONES DE LECTURA ====================

    /**
     * Obtiene los datos del estudiante por matrícula.
     * Requiere permiso READ.
     */
    suspend fun getStudent(matricula: String): ProviderResult<Student?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Consultando estudiante: $matricula")

            val uri = SicenetProviderContract.Student.CONTENT_URI.buildUpon()
                .appendPath(matricula)
                .build()

            val projection = arrayOf(
                SicenetProviderContract.Student.MATRICULA,
                SicenetProviderContract.Student.NOMBRE,
                SicenetProviderContract.Student.APELLIDOS,
                SicenetProviderContract.Student.CARRERA,
                SicenetProviderContract.Student.SEMESTRE,
                SicenetProviderContract.Student.PROMEDIO,
                SicenetProviderContract.Student.ESTADO,
                SicenetProviderContract.Student.FOTO_URL,
                SicenetProviderContract.Student.ESPECIALIDAD,
                SicenetProviderContract.Student.CDTS_REUNIDOS,
                SicenetProviderContract.Student.CDTS_ACTUALES,
                SicenetProviderContract.Student.SEM_ACTUAL,
                SicenetProviderContract.Student.INSCRITO,
                SicenetProviderContract.Student.ESTATUS_ACADEMICO,
                SicenetProviderContract.Student.ESTATUS_ALUMNO,
                SicenetProviderContract.Student.LAST_UPDATE
            )

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val student = Student(
                        matricula = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.MATRICULA)) ?: "",
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.NOMBRE)) ?: "",
                        apellidos = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.APELLIDOS)) ?: "",
                        carrera = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.CARRERA)) ?: "",
                        semestre = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.SEMESTRE)),
                        promedio = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.PROMEDIO)) ?: "",
                        estado = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.ESTADO)) ?: "",
                        fotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.FOTO_URL)) ?: "",
                        especialidad = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.ESPECIALIDAD)) ?: "",
                        cdtsReunidos = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.CDTS_REUNIDOS)),
                        cdtsActuales = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.CDTS_ACTUALES)),
                        semActual = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.SEM_ACTUAL)),
                        inscrito = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.INSCRITO)) ?: "",
                        estatusAcademico = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.ESTATUS_ACADEMICO)) ?: "",
                        estatusAlumno = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.ESTATUS_ALUMNO)) ?: "",
                        lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(SicenetProviderContract.Student.LAST_UPDATE))
                    )
                    ProviderResult.Success(student)
                } else {
                    ProviderResult.Success(null)
                }
            } ?: ProviderResult.Error("No se pudo realizar la consulta")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al consultar estudiante: ${e.message}")
            ProviderResult.Error("Permiso de lectura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al consultar estudiante: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Obtiene el kardex del estudiante.
     * Requiere permiso READ.
     */
    suspend fun getKardex(matricula: String): ProviderResult<List<KardexEntry>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Consultando kardex: $matricula")

            val projection = arrayOf(
                SicenetProviderContract.Kardex.MATRICULA,
                SicenetProviderContract.Kardex.CLAVE,
                SicenetProviderContract.Kardex.NOMBRE,
                SicenetProviderContract.Kardex.CALIFICACION,
                SicenetProviderContract.Kardex.ACREDITACION,
                SicenetProviderContract.Kardex.PERIODO,
                SicenetProviderContract.Kardex.LAST_UPDATE
            )

            val selectionArgs = arrayOf(matricula)

            contentResolver.query(
                SicenetProviderContract.Kardex.CONTENT_URI,
                projection,
                null,
                selectionArgs,
                null
            )?.use { cursor ->
                val kardexList = mutableListOf<KardexEntry>()
                while (cursor.moveToNext()) {
                    kardexList.add(
                        KardexEntry(
                            matricula = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.MATRICULA)) ?: "",
                            clave = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.CLAVE)) ?: "",
                            nombre = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.NOMBRE)) ?: "",
                            calificacion = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.CALIFICACION)),
                            acreditacion = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.ACREDITACION)) ?: "",
                            periodo = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.PERIODO)) ?: "",
                            lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(SicenetProviderContract.Kardex.LAST_UPDATE))
                        )
                    )
                }
                ProviderResult.Success(kardexList)
            } ?: ProviderResult.Error("No se pudo realizar la consulta")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al consultar kardex: ${e.message}")
            ProviderResult.Error("Permiso de lectura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al consultar kardex: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Obtiene la carga académica del estudiante.
     * Requiere permiso READ.
     */
    suspend fun getCarga(matricula: String): ProviderResult<List<CargaEntry>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Consultando carga académica: $matricula")

            val projection = arrayOf(
                SicenetProviderContract.Carga.MATRICULA,
                SicenetProviderContract.Carga.NOMBRE,
                SicenetProviderContract.Carga.DOCENTE,
                SicenetProviderContract.Carga.GRUPO,
                SicenetProviderContract.Carga.CREDITOS,
                SicenetProviderContract.Carga.LUNES,
                SicenetProviderContract.Carga.MARTES,
                SicenetProviderContract.Carga.MIERCOLES,
                SicenetProviderContract.Carga.JUEVES,
                SicenetProviderContract.Carga.VIERNES,
                SicenetProviderContract.Carga.SABADO,
                SicenetProviderContract.Carga.LAST_UPDATE
            )

            val selectionArgs = arrayOf(matricula)

            contentResolver.query(
                SicenetProviderContract.Carga.CONTENT_URI,
                projection,
                null,
                selectionArgs,
                null
            )?.use { cursor ->
                val cargaList = mutableListOf<CargaEntry>()
                while (cursor.moveToNext()) {
                    cargaList.add(
                        CargaEntry(
                            matricula = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.MATRICULA)) ?: "",
                            nombre = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.NOMBRE)) ?: "",
                            docente = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.DOCENTE)) ?: "",
                            grupo = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.GRUPO)) ?: "",
                            creditos = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.CREDITOS)),
                            lunes = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.LUNES)) ?: "",
                            martes = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.MARTES)) ?: "",
                            miercoles = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.MIERCOLES)) ?: "",
                            jueves = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.JUEVES)) ?: "",
                            viernes = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.VIERNES)) ?: "",
                            sabado = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.SABADO)) ?: "",
                            lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(SicenetProviderContract.Carga.LAST_UPDATE))
                        )
                    )
                }
                ProviderResult.Success(cargaList)
            } ?: ProviderResult.Error("No se pudo realizar la consulta")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al consultar carga: ${e.message}")
            ProviderResult.Error("Permiso de lectura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al consultar carga: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Obtiene las calificaciones por unidad del estudiante.
     * Requiere permiso READ.
     */
    suspend fun getCalifUnidad(matricula: String): ProviderResult<List<CalifUnidad>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Consultando calificaciones por unidad: $matricula")

            val projection = arrayOf(
                SicenetProviderContract.CalifUnidad.MATRICULA,
                SicenetProviderContract.CalifUnidad.MATERIA,
                SicenetProviderContract.CalifUnidad.PARCIALES,
                SicenetProviderContract.CalifUnidad.LAST_UPDATE
            )

            val selectionArgs = arrayOf(matricula)

            contentResolver.query(
                SicenetProviderContract.CalifUnidad.CONTENT_URI,
                projection,
                null,
                selectionArgs,
                null
            )?.use { cursor ->
                val califList = mutableListOf<CalifUnidad>()
                while (cursor.moveToNext()) {
                    val parcialesStr = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifUnidad.PARCIALES)) ?: ""
                    califList.add(
                        CalifUnidad(
                            matricula = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifUnidad.MATRICULA)) ?: "",
                            materia = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifUnidad.MATERIA)) ?: "",
                            parciales = parcialesStr.split(",").map { it.trim() },
                            lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifUnidad.LAST_UPDATE))
                        )
                    )
                }
                ProviderResult.Success(califList)
            } ?: ProviderResult.Error("No se pudo realizar la consulta")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al consultar calificaciones: ${e.message}")
            ProviderResult.Error("Permiso de lectura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al consultar calificaciones: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Obtiene las calificaciones finales del estudiante.
     * Requiere permiso READ.
     */
    suspend fun getCalifFinal(matricula: String): ProviderResult<List<CalifFinal>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Consultando calificaciones finales: $matricula")

            val projection = arrayOf(
                SicenetProviderContract.CalifFinal.MATRICULA,
                SicenetProviderContract.CalifFinal.MATERIA,
                SicenetProviderContract.CalifFinal.CALIF,
                SicenetProviderContract.CalifFinal.LAST_UPDATE
            )

            val selectionArgs = arrayOf(matricula)

            contentResolver.query(
                SicenetProviderContract.CalifFinal.CONTENT_URI,
                projection,
                null,
                selectionArgs,
                null
            )?.use { cursor ->
                val califList = mutableListOf<CalifFinal>()
                while (cursor.moveToNext()) {
                    califList.add(
                        CalifFinal(
                            matricula = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifFinal.MATRICULA)) ?: "",
                            materia = cursor.getString(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifFinal.MATERIA)) ?: "",
                            calif = cursor.getInt(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifFinal.CALIF)),
                            lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(SicenetProviderContract.CalifFinal.LAST_UPDATE))
                        )
                    )
                }
                ProviderResult.Success(califList)
            } ?: ProviderResult.Error("No se pudo realizar la consulta")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al consultar calificaciones finales: ${e.message}")
            ProviderResult.Error("Permiso de lectura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al consultar calificaciones finales: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    // ==================== OPERACIONES DE ESCRITURA ====================

    /**
     * Inserta un registro en el kardex.
     * Requiere permiso WRITE.
     */
    suspend fun insertKardexEntry(entry: KardexEntry): ProviderResult<Uri> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Insertando kardex: ${entry.matricula} / ${entry.clave}")

            val values = ContentValues().apply {
                put(SicenetProviderContract.Kardex.MATRICULA, entry.matricula)
                put(SicenetProviderContract.Kardex.CLAVE, entry.clave)
                put(SicenetProviderContract.Kardex.NOMBRE, entry.nombre)
                put(SicenetProviderContract.Kardex.CALIFICACION, entry.calificacion)
                put(SicenetProviderContract.Kardex.ACREDITACION, entry.acreditacion)
                put(SicenetProviderContract.Kardex.PERIODO, entry.periodo)
                put(SicenetProviderContract.Kardex.LAST_UPDATE, System.currentTimeMillis())
            }

            val uri = contentResolver.insert(SicenetProviderContract.Kardex.CONTENT_URI, values)
            if (uri != null) {
                ProviderResult.Success(uri)
            } else {
                ProviderResult.Error("No se pudo insertar el registro")
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al insertar kardex: ${e.message}")
            ProviderResult.Error("Permiso de escritura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar kardex: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Elimina todos los registros de kardex para una matrícula.
     * Requiere permiso WRITE.
     */
    suspend fun deleteKardex(matricula: String): ProviderResult<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Eliminando kardex: $matricula")

            val selectionArgs = arrayOf(matricula)
            val deleted = contentResolver.delete(
                SicenetProviderContract.Kardex.CONTENT_URI,
                null,
                selectionArgs
            )
            ProviderResult.Success(deleted)

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException al eliminar kardex: ${e.message}")
            ProviderResult.Error("Permiso de escritura denegado", securityException = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar kardex: ${e.message}")
            ProviderResult.Error("Error: ${e.message}")
        }
    }
}
