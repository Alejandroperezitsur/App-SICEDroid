package com.example.marsphotos.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.marsphotos.data.local.AppDatabase
import com.example.marsphotos.data.local.CalifFinalEntity
import com.example.marsphotos.data.local.CalifUnidadEntity
import com.example.marsphotos.data.local.CargaEntity
import com.example.marsphotos.data.local.KardexEntity
import com.example.marsphotos.data.local.StudentDao
import com.example.marsphotos.data.local.StudentEntity
import kotlinx.coroutines.runBlocking

/**
 * Content Provider que expone los datos académicos de SICEDroid.
 * Permite consultar y modificar: Student, Kardex, Carga Académica, Calificaciones.
 *
 * Permisos requeridos:
 * - com.example.marsphotos.provider.READ para operaciones de lectura (query)
 * - com.example.marsphotos.provider.WRITE para operaciones de escritura (insert, update, delete)
 */
class SicenetContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "SicenetContentProvider"

        /** URI Matcher para identificar las URIs entrantes */
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Student URIs
            addURI(SicenetContract.CONTENT_AUTHORITY, SicenetContract.Paths.STUDENT, SicenetContract.UriCodes.STUDENT)
            addURI(SicenetContract.CONTENT_AUTHORITY, "${SicenetContract.Paths.STUDENT}/*", SicenetContract.UriCodes.STUDENT_ID)

            // Kardex URIs
            addURI(SicenetContract.CONTENT_AUTHORITY, SicenetContract.Paths.KARDEX, SicenetContract.UriCodes.KARDEX)
            addURI(SicenetContract.CONTENT_AUTHORITY, "${SicenetContract.Paths.KARDEX}/*", SicenetContract.UriCodes.KARDEX_ID)

            // Carga URIs
            addURI(SicenetContract.CONTENT_AUTHORITY, SicenetContract.Paths.CARGA, SicenetContract.UriCodes.CARGA)
            addURI(SicenetContract.CONTENT_AUTHORITY, "${SicenetContract.Paths.CARGA}/*", SicenetContract.UriCodes.CARGA_ID)

            // CalifUnidad URIs
            addURI(SicenetContract.CONTENT_AUTHORITY, SicenetContract.Paths.CALIF_UNIDAD, SicenetContract.UriCodes.CALIF_UNIDAD)
            addURI(SicenetContract.CONTENT_AUTHORITY, "${SicenetContract.Paths.CALIF_UNIDAD}/*", SicenetContract.UriCodes.CALIF_UNIDAD_ID)

            // CalifFinal URIs
            addURI(SicenetContract.CONTENT_AUTHORITY, SicenetContract.Paths.CALIF_FINAL, SicenetContract.UriCodes.CALIF_FINAL)
            addURI(SicenetContract.CONTENT_AUTHORITY, "${SicenetContract.Paths.CALIF_FINAL}/*", SicenetContract.UriCodes.CALIF_FINAL_ID)
        }
    }

    private lateinit var studentDao: StudentDao

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: Inicializando Content Provider")
        context?.let { ctx ->
            studentDao = AppDatabase.getDatabase(ctx).studentDao()
            Log.d(TAG, "onCreate: DAO inicializado correctamente")
        }
        return true
    }

    /**
     * Consulta datos del Content Provider.
     * Requiere permiso: com.example.marsphotos.provider.READ
     */
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: URI=$uri, selection=$selection")

        // Verificar permiso de lectura
        context?.enforceCallingOrSelfPermission(SicenetContract.PERMISSION_READ, null)
            ?: throw SecurityException("Se requiere permiso READ para consultar datos")

        return runBlocking {
            when (uriMatcher.match(uri)) {
                SicenetContract.UriCodes.STUDENT -> queryStudent(projection, selection, selectionArgs)
                SicenetContract.UriCodes.STUDENT_ID -> {
                    val matricula = uri.lastPathSegment ?: ""
                    queryStudent(projection, "${SicenetContract.Student.MATRICULA}=?", arrayOf(matricula))
                }
                SicenetContract.UriCodes.KARDEX -> {
                    val matricula = selectionArgs?.firstOrNull() ?: ""
                    queryKardex(projection, matricula)
                }
                SicenetContract.UriCodes.CARGA -> {
                    val matricula = selectionArgs?.firstOrNull() ?: ""
                    queryCarga(projection, matricula)
                }
                SicenetContract.UriCodes.CALIF_UNIDAD -> {
                    val matricula = selectionArgs?.firstOrNull() ?: ""
                    queryCalifUnidad(projection, matricula)
                }
                SicenetContract.UriCodes.CALIF_FINAL -> {
                    val matricula = selectionArgs?.firstOrNull() ?: ""
                    queryCalifFinal(projection, matricula)
                }
                else -> throw IllegalArgumentException("URI desconocida: $uri")
            }
        }
    }

    private suspend fun queryStudent(
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Cursor {
        val cols = projection ?: SicenetContract.Student.DEFAULT_PROJECTION
        val cursor = MatrixCursor(cols)

        // Obtener estudiante de la base de datos
        val matricula = selectionArgs?.firstOrNull()
        val student = if (matricula != null) {
            studentDao.getStudentSync(matricula)
        } else {
            // Obtener el primer estudiante disponible
            null // Simplified - in production would query all
        }

        student?.let { s ->
            val row = arrayOfNulls<Any>(cols.size)
            cols.forEachIndexed { index, col ->
                row[index] = when (col) {
                    SicenetContract.Student.MATRICULA -> s.matricula
                    SicenetContract.Student.NOMBRE -> s.nombre
                    SicenetContract.Student.APELLIDOS -> s.apellidos
                    SicenetContract.Student.CARRERA -> s.carrera
                    SicenetContract.Student.SEMESTRE -> s.semestre
                    SicenetContract.Student.PROMEDIO -> s.promedio
                    SicenetContract.Student.ESTADO -> s.estado
                    SicenetContract.Student.FOTO_URL -> s.fotoUrl
                    SicenetContract.Student.ESPECIALIDAD -> s.especialidad
                    SicenetContract.Student.CDTS_REUNIDOS -> s.cdtsReunidos
                    SicenetContract.Student.CDTS_ACTUALES -> s.cdtsActuales
                    SicenetContract.Student.SEM_ACTUAL -> s.semActual
                    SicenetContract.Student.INSCRITO -> s.inscrito
                    SicenetContract.Student.ESTATUS_ACADEMICO -> s.estatusAcademico
                    SicenetContract.Student.ESTATUS_ALUMNO -> s.estatusAlumno
                    SicenetContract.Student.REINSCRIPCION_FECHA -> s.reinscripcionFecha
                    SicenetContract.Student.SIN_ADEUDOS -> s.sinAdeudos
                    SicenetContract.Student.LINEAMIENTO -> s.lineamiento
                    SicenetContract.Student.MOD_EDUCATIVO -> s.modEducativo
                    SicenetContract.Student.LAST_UPDATE -> s.lastUpdate
                    else -> null
                }
            }
            cursor.addRow(row)
        }

        return cursor
    }

    private suspend fun queryKardex(projection: Array<out String>?, matricula: String): Cursor {
        val cols = projection ?: SicenetContract.Kardex.DEFAULT_PROJECTION
        val cursor = MatrixCursor(cols)

        val kardexList = studentDao.getKardexSync(matricula)
        kardexList.forEach { k ->
            val row = arrayOfNulls<Any>(cols.size)
            cols.forEachIndexed { index, col ->
                row[index] = when (col) {
                    SicenetContract.Kardex.MATRICULA -> k.matricula
                    SicenetContract.Kardex.CLAVE -> k.clave
                    SicenetContract.Kardex.NOMBRE -> k.nombre
                    SicenetContract.Kardex.CALIFICACION -> k.calificacion
                    SicenetContract.Kardex.ACREDITACION -> k.acreditacion
                    SicenetContract.Kardex.PERIODO -> k.periodo
                    SicenetContract.Kardex.LAST_UPDATE -> k.lastUpdate
                    else -> null
                }
            }
            cursor.addRow(row)
        }

        return cursor
    }

    private suspend fun queryCarga(projection: Array<out String>?, matricula: String): Cursor {
        val cols = projection ?: SicenetContract.Carga.DEFAULT_PROJECTION
        val cursor = MatrixCursor(cols)

        val cargaList = studentDao.getCargaSync(matricula)
        cargaList.forEach { c ->
            val row = arrayOfNulls<Any>(cols.size)
            cols.forEachIndexed { index, col ->
                row[index] = when (col) {
                    SicenetContract.Carga.MATRICULA -> c.matricula
                    SicenetContract.Carga.NOMBRE -> c.nombre
                    SicenetContract.Carga.DOCENTE -> c.docente
                    SicenetContract.Carga.GRUPO -> c.grupo
                    SicenetContract.Carga.CREDITOS -> c.creditos
                    SicenetContract.Carga.LUNES -> c.lunes
                    SicenetContract.Carga.MARTES -> c.martes
                    SicenetContract.Carga.MIERCOLES -> c.miercoles
                    SicenetContract.Carga.JUEVES -> c.jueves
                    SicenetContract.Carga.VIERNES -> c.viernes
                    SicenetContract.Carga.SABADO -> c.sabado
                    SicenetContract.Carga.LAST_UPDATE -> c.lastUpdate
                    else -> null
                }
            }
            cursor.addRow(row)
        }

        return cursor
    }

    private suspend fun queryCalifUnidad(projection: Array<out String>?, matricula: String): Cursor {
        val cols = projection ?: SicenetContract.CalifUnidad.DEFAULT_PROJECTION
        val cursor = MatrixCursor(cols)

        val califList = studentDao.getCalifUnidadSync(matricula)
        califList.forEach { c ->
            val row = arrayOfNulls<Any>(cols.size)
            cols.forEachIndexed { index, col ->
                row[index] = when (col) {
                    SicenetContract.CalifUnidad.MATRICULA -> c.matricula
                    SicenetContract.CalifUnidad.MATERIA -> c.materia
                    SicenetContract.CalifUnidad.PARCIALES -> c.parciales.joinToString(",")
                    SicenetContract.CalifUnidad.LAST_UPDATE -> c.lastUpdate
                    else -> null
                }
            }
            cursor.addRow(row)
        }

        return cursor
    }

    private suspend fun queryCalifFinal(projection: Array<out String>?, matricula: String): Cursor {
        val cols = projection ?: SicenetContract.CalifFinal.DEFAULT_PROJECTION
        val cursor = MatrixCursor(cols)

        val califList = studentDao.getCalifFinalSync(matricula)
        califList.forEach { c ->
            val row = arrayOfNulls<Any>(cols.size)
            cols.forEachIndexed { index, col ->
                row[index] = when (col) {
                    SicenetContract.CalifFinal.MATRICULA -> c.matricula
                    SicenetContract.CalifFinal.MATERIA -> c.materia
                    SicenetContract.CalifFinal.CALIF -> c.calif
                    SicenetContract.CalifFinal.LAST_UPDATE -> c.lastUpdate
                    else -> null
                }
            }
            cursor.addRow(row)
        }

        return cursor
    }

    /**
     * Inserta datos en el Content Provider.
     * Requiere permiso: com.example.marsphotos.provider.WRITE
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: URI=$uri")

        // Verificar permiso de escritura
        context?.enforceCallingOrSelfPermission(SicenetContract.PERMISSION_WRITE, null)
            ?: throw SecurityException("Se requiere permiso WRITE para insertar datos")

        values ?: throw IllegalArgumentException("ContentValues no puede ser null")

        return runBlocking {
            when (uriMatcher.match(uri)) {
                SicenetContract.UriCodes.STUDENT -> insertStudent(values)
                SicenetContract.UriCodes.KARDEX -> insertKardex(values)
                SicenetContract.UriCodes.CARGA -> insertCarga(values)
                SicenetContract.UriCodes.CALIF_UNIDAD -> insertCalifUnidad(values)
                SicenetContract.UriCodes.CALIF_FINAL -> insertCalifFinal(values)
                else -> throw IllegalArgumentException("URI desconocida para insert: $uri")
            }
        }
    }

    private suspend fun insertStudent(values: ContentValues): Uri? {
        val student = StudentEntity(
            matricula = values.getAsString(SicenetContract.Student.MATRICULA) ?: "",
            nombre = values.getAsString(SicenetContract.Student.NOMBRE) ?: "",
            apellidos = values.getAsString(SicenetContract.Student.APELLIDOS) ?: "",
            carrera = values.getAsString(SicenetContract.Student.CARRERA) ?: "",
            semestre = values.getAsInteger(SicenetContract.Student.SEMESTRE) ?: 0,
            promedio = values.getAsString(SicenetContract.Student.PROMEDIO) ?: "",
            estado = values.getAsString(SicenetContract.Student.ESTADO) ?: "",
            fotoUrl = values.getAsString(SicenetContract.Student.FOTO_URL) ?: "",
            especialidad = values.getAsString(SicenetContract.Student.ESPECIALIDAD) ?: "",
            cdtsReunidos = values.getAsInteger(SicenetContract.Student.CDTS_REUNIDOS) ?: 0,
            cdtsActuales = values.getAsInteger(SicenetContract.Student.CDTS_ACTUALES) ?: 0,
            semActual = values.getAsInteger(SicenetContract.Student.SEM_ACTUAL) ?: 0,
            inscrito = values.getAsString(SicenetContract.Student.INSCRITO) ?: "",
            estatusAcademico = values.getAsString(SicenetContract.Student.ESTATUS_ACADEMICO) ?: "",
            estatusAlumno = values.getAsString(SicenetContract.Student.ESTATUS_ALUMNO) ?: "",
            reinscripcionFecha = values.getAsString(SicenetContract.Student.REINSCRIPCION_FECHA) ?: "",
            sinAdeudos = values.getAsString(SicenetContract.Student.SIN_ADEUDOS) ?: "",
            lineamiento = values.getAsInteger(SicenetContract.Student.LINEAMIENTO) ?: 0,
            modEducativo = values.getAsInteger(SicenetContract.Student.MOD_EDUCATIVO) ?: 0,
            lastUpdate = values.getAsLong(SicenetContract.Student.LAST_UPDATE) ?: System.currentTimeMillis()
        )

        studentDao.insertStudent(student)
        Log.d(TAG, "insertStudent: Estudiante insertado - ${student.matricula}")

        return SicenetContract.Student.CONTENT_URI.buildUpon().appendPath(student.matricula).build()
    }

    private suspend fun insertKardex(values: ContentValues): Uri? {
        val matricula = values.getAsString(SicenetContract.Kardex.MATRICULA) ?: ""
        val clave = values.getAsString(SicenetContract.Kardex.CLAVE) ?: ""

        val kardex = KardexEntity(
            matricula = matricula,
            clave = clave,
            nombre = values.getAsString(SicenetContract.Kardex.NOMBRE) ?: "",
            calificacion = values.getAsInteger(SicenetContract.Kardex.CALIFICACION) ?: 0,
            acreditacion = values.getAsString(SicenetContract.Kardex.ACREDITACION) ?: "",
            periodo = values.getAsString(SicenetContract.Kardex.PERIODO) ?: "",
            lastUpdate = values.getAsLong(SicenetContract.Kardex.LAST_UPDATE) ?: System.currentTimeMillis()
        )

        studentDao.insertKardex(listOf(kardex))
        Log.d(TAG, "insertKardex: Kardex insertado - $matricula / $clave")

        return SicenetContract.Kardex.CONTENT_URI.buildUpon().appendPath("$matricula/$clave").build()
    }

    private suspend fun insertCarga(values: ContentValues): Uri? {
        val matricula = values.getAsString(SicenetContract.Carga.MATRICULA) ?: ""
        val nombre = values.getAsString(SicenetContract.Carga.NOMBRE) ?: ""
        val grupo = values.getAsString(SicenetContract.Carga.GRUPO) ?: ""

        val carga = CargaEntity(
            matricula = matricula,
            nombre = nombre,
            docente = values.getAsString(SicenetContract.Carga.DOCENTE) ?: "",
            grupo = grupo,
            creditos = values.getAsInteger(SicenetContract.Carga.CREDITOS) ?: 0,
            lunes = values.getAsString(SicenetContract.Carga.LUNES) ?: "",
            martes = values.getAsString(SicenetContract.Carga.MARTES) ?: "",
            miercoles = values.getAsString(SicenetContract.Carga.MIERCOLES) ?: "",
            jueves = values.getAsString(SicenetContract.Carga.JUEVES) ?: "",
            viernes = values.getAsString(SicenetContract.Carga.VIERNES) ?: "",
            sabado = values.getAsString(SicenetContract.Carga.SABADO) ?: "",
            lastUpdate = values.getAsLong(SicenetContract.Carga.LAST_UPDATE) ?: System.currentTimeMillis()
        )

        studentDao.insertCarga(listOf(carga))
        Log.d(TAG, "insertCarga: Carga insertada - $matricula / $nombre / $grupo")

        return SicenetContract.Carga.CONTENT_URI.buildUpon().appendPath("$matricula/$nombre/$grupo").build()
    }

    private suspend fun insertCalifUnidad(values: ContentValues): Uri? {
        val matricula = values.getAsString(SicenetContract.CalifUnidad.MATRICULA) ?: ""
        val materia = values.getAsString(SicenetContract.CalifUnidad.MATERIA) ?: ""

        val parcialesStr = values.getAsString(SicenetContract.CalifUnidad.PARCIALES) ?: ""
        val parciales = parcialesStr.split(",").map { it.trim() }

        val calif = CalifUnidadEntity(
            matricula = matricula,
            materia = materia,
            parciales = parciales,
            lastUpdate = values.getAsLong(SicenetContract.CalifUnidad.LAST_UPDATE) ?: System.currentTimeMillis()
        )

        studentDao.insertCalifUnidad(listOf(calif))
        Log.d(TAG, "insertCalifUnidad: Calificación insertada - $matricula / $materia")

        return SicenetContract.CalifUnidad.CONTENT_URI.buildUpon().appendPath("$matricula/$materia").build()
    }

    private suspend fun insertCalifFinal(values: ContentValues): Uri? {
        val matricula = values.getAsString(SicenetContract.CalifFinal.MATRICULA) ?: ""
        val materia = values.getAsString(SicenetContract.CalifFinal.MATERIA) ?: ""

        val calif = CalifFinalEntity(
            matricula = matricula,
            materia = materia,
            calif = values.getAsInteger(SicenetContract.CalifFinal.CALIF) ?: 0,
            lastUpdate = values.getAsLong(SicenetContract.CalifFinal.LAST_UPDATE) ?: System.currentTimeMillis()
        )

        studentDao.insertCalifFinal(listOf(calif))
        Log.d(TAG, "insertCalifFinal: Calificación final insertada - $matricula / $materia")

        return SicenetContract.CalifFinal.CONTENT_URI.buildUpon().appendPath("$matricula/$materia").build()
    }

    /**
     * Actualiza datos en el Content Provider.
     * Requiere permiso: com.example.marsphotos.provider.WRITE
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        Log.d(TAG, "update: URI=$uri")

        // Verificar permiso de escritura
        context?.enforceCallingOrSelfPermission(SicenetContract.PERMISSION_WRITE, null)
            ?: throw SecurityException("Se requiere permiso WRITE para actualizar datos")

        values ?: return 0

        return runBlocking {
            when (uriMatcher.match(uri)) {
                SicenetContract.UriCodes.STUDENT_ID -> {
                    val matricula = uri.lastPathSegment ?: return@runBlocking 0
                    updateStudent(matricula, values)
                }
                SicenetContract.UriCodes.KARDEX -> updateKardex(values, selectionArgs)
                SicenetContract.UriCodes.CARGA -> updateCarga(values, selectionArgs)
                SicenetContract.UriCodes.CALIF_UNIDAD -> updateCalifUnidad(values, selectionArgs)
                SicenetContract.UriCodes.CALIF_FINAL -> updateCalifFinal(values, selectionArgs)
                else -> throw IllegalArgumentException("URI desconocida para update: $uri")
            }
        }
    }

    private suspend fun updateStudent(matricula: String, values: ContentValues): Int {
        val existing = studentDao.getStudentSync(matricula) ?: return 0

        val updated = existing.copy(
            nombre = values.getAsString(SicenetContract.Student.NOMBRE) ?: existing.nombre,
            apellidos = values.getAsString(SicenetContract.Student.APELLIDOS) ?: existing.apellidos,
            carrera = values.getAsString(SicenetContract.Student.CARRERA) ?: existing.carrera,
            semestre = values.getAsInteger(SicenetContract.Student.SEMESTRE) ?: existing.semestre,
            promedio = values.getAsString(SicenetContract.Student.PROMEDIO) ?: existing.promedio,
            fotoUrl = values.getAsString(SicenetContract.Student.FOTO_URL) ?: existing.fotoUrl,
            lastUpdate = System.currentTimeMillis()
        )

        studentDao.insertStudent(updated)
        Log.d(TAG, "updateStudent: Estudiante actualizado - $matricula")
        return 1
    }

    private suspend fun updateKardex(values: ContentValues, selectionArgs: Array<out String>?): Int {
        // Implementación simplificada - en producción sería más compleja
        return 0
    }

    private suspend fun updateCarga(values: ContentValues, selectionArgs: Array<out String>?): Int {
        return 0
    }

    private suspend fun updateCalifUnidad(values: ContentValues, selectionArgs: Array<out String>?): Int {
        return 0
    }

    private suspend fun updateCalifFinal(values: ContentValues, selectionArgs: Array<out String>?): Int {
        return 0
    }

    /**
     * Elimina datos del Content Provider.
     * Requiere permiso: com.example.marsphotos.provider.WRITE
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: URI=$uri")

        // Verificar permiso de escritura
        context?.enforceCallingOrSelfPermission(SicenetContract.PERMISSION_WRITE, null)
            ?: throw SecurityException("Se requiere permiso WRITE para eliminar datos")

        return runBlocking {
            when (uriMatcher.match(uri)) {
                SicenetContract.UriCodes.STUDENT_ID -> {
                    val matricula = uri.lastPathSegment ?: return@runBlocking 0
                    deleteStudent(matricula)
                }
                SicenetContract.UriCodes.KARDEX -> {
                    val matricula = selectionArgs?.firstOrNull() ?: return@runBlocking 0
                    studentDao.deleteKardex(matricula)
                    Log.d(TAG, "delete: Kardex eliminado para $matricula")
                    1
                }
                SicenetContract.UriCodes.CARGA -> {
                    val matricula = selectionArgs?.firstOrNull() ?: return@runBlocking 0
                    studentDao.deleteCarga(matricula)
                    Log.d(TAG, "delete: Carga eliminada para $matricula")
                    1
                }
                SicenetContract.UriCodes.CALIF_UNIDAD -> {
                    val matricula = selectionArgs?.firstOrNull() ?: return@runBlocking 0
                    studentDao.deleteCalifUnidad(matricula)
                    Log.d(TAG, "delete: CalifUnidad eliminada para $matricula")
                    1
                }
                SicenetContract.UriCodes.CALIF_FINAL -> {
                    val matricula = selectionArgs?.firstOrNull() ?: return@runBlocking 0
                    studentDao.deleteCalifFinal(matricula)
                    Log.d(TAG, "delete: CalifFinal eliminada para $matricula")
                    1
                }
                else -> throw IllegalArgumentException("URI desconocida para delete: $uri")
            }
        }
    }

    private suspend fun deleteStudent(matricula: String): Int {
        // Eliminar estudiante y todos sus datos relacionados
        studentDao.deleteKardex(matricula)
        studentDao.deleteCarga(matricula)
        studentDao.deleteCalifUnidad(matricula)
        studentDao.deleteCalifFinal(matricula)
        // Nota: La eliminación directa del estudiante requeriría un método adicional en el DAO
        Log.d(TAG, "deleteStudent: Datos del estudiante eliminados - $matricula")
        return 1
    }

    /**
     * Retorna el MIME type para una URI dada.
     */
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            SicenetContract.UriCodes.STUDENT -> SicenetContract.Student.CONTENT_TYPE
            SicenetContract.UriCodes.STUDENT_ID -> SicenetContract.Student.CONTENT_ITEM_TYPE
            SicenetContract.UriCodes.KARDEX -> SicenetContract.Kardex.CONTENT_TYPE
            SicenetContract.UriCodes.KARDEX_ID -> SicenetContract.Kardex.CONTENT_ITEM_TYPE
            SicenetContract.UriCodes.CARGA -> SicenetContract.Carga.CONTENT_TYPE
            SicenetContract.UriCodes.CARGA_ID -> SicenetContract.Carga.CONTENT_ITEM_TYPE
            SicenetContract.UriCodes.CALIF_UNIDAD -> SicenetContract.CalifUnidad.CONTENT_TYPE
            SicenetContract.UriCodes.CALIF_UNIDAD_ID -> SicenetContract.CalifUnidad.CONTENT_ITEM_TYPE
            SicenetContract.UriCodes.CALIF_FINAL -> SicenetContract.CalifFinal.CONTENT_TYPE
            SicenetContract.UriCodes.CALIF_FINAL_ID -> SicenetContract.CalifFinal.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }
}
