package com.example.sicedroid.db

import com.example.sicedroid.currentTimeMillis
import com.example.sicedroid.model.*
import kotlinx.serialization.json.Json

class LocalDataSource(driverFactory: DriverFactory) {

    private val database = SicenetDatabase(driverFactory.createDriver())
    private val json = Json { ignoreUnknownKeys = true }

    // ── Session ────────────────────────────────────────────────────────

    fun getSession(): SessionEntity? {
        return database.sicenetDatabaseQueries.getSession().executeAsOneOrNull()
    }

    fun sessionExists(): Boolean {
        return database.sicenetDatabaseQueries.sessionExists().executeAsOne() > 0
    }

    fun saveSession(matricula: String, password: String) {
        database.sicenetDatabaseQueries.saveSession(
            matricula = matricula,
            password = password,
            last_login = currentTimeMillis()
        )
    }

    fun clearSession() {
        database.sicenetDatabaseQueries.clearSession()
    }

    // ── Profile ────────────────────────────────────────────────────────

    fun getProfile(matricula: String): ProfileStudent? {
        val entity = database.sicenetDatabaseQueries.getProfile(matricula).executeAsOneOrNull()
            ?: return null
        return ProfileStudent(
            matricula = entity.matricula,
            nombre = entity.nombre,
            apellidos = entity.apellidos,
            carrera = entity.carrera,
            especialidad = entity.especialidad,
            semestre = entity.semestre,
            promedio = entity.promedio,
            estado = entity.estado,
            statusMatricula = entity.status_matricula,
            cdtsReunidos = entity.cdts_reunidos,
            cdtsActuales = entity.cdts_actuales,
            semActual = entity.sem_actual,
            inscrito = entity.inscrito,
            reinscripcionFecha = entity.reinscripcion_fecha,
            estatusAlumno = entity.estatus_alumno,
            estatusAcademico = entity.estatus_academico,
            fotoUrl = entity.foto_url,
            sinAdeudos = entity.sin_adeudos,
            lineamiento = entity.lineamiento,
            modEducativo = entity.mod_educativo,
            operaciones = json.decodeFromString(entity.operaciones)
        )
    }

    fun saveProfile(matricula: String, profile: ProfileStudent) {
        database.sicenetDatabaseQueries.insertProfile(
            matricula = matricula,
            nombre = profile.nombre,
            apellidos = profile.apellidos,
            carrera = profile.carrera,
            especialidad = profile.especialidad,
            semestre = profile.semestre,
            promedio = profile.promedio,
            estado = profile.estado,
            status_matricula = profile.statusMatricula,
            cdts_reunidos = profile.cdtsReunidos,
            cdts_actuales = profile.cdtsActuales,
            sem_actual = profile.semActual,
            inscrito = profile.inscrito,
            reinscripcion_fecha = profile.reinscripcionFecha,
            estatus_alumno = profile.estatusAlumno,
            estatus_academico = profile.estatusAcademico,
            foto_url = profile.fotoUrl,
            sin_adeudos = profile.sinAdeudos,
            lineamiento = profile.lineamiento,
            mod_educativo = profile.modEducativo,
            operaciones = json.encodeToString(profile.operaciones),
            last_update = currentTimeMillis()
        )
    }

    fun deleteProfile(matricula: String) {
        database.sicenetDatabaseQueries.deleteProfile(matricula)
    }

    // ── Kardex ─────────────────────────────────────────────────────────

    fun getKardex(matricula: String): List<MateriaKardex> {
        return database.sicenetDatabaseQueries.getKardex(matricula).executeAsList().map { entity ->
            MateriaKardex(
                clave = entity.clave,
                nombre = entity.nombre,
                calificacion = entity.calificacion,
                acreditacion = entity.acreditacion,
                periodo = entity.periodo
            )
        }
    }

    fun saveKardex(matricula: String, kardex: List<MateriaKardex>) {
        database.transaction {
            database.sicenetDatabaseQueries.deleteKardex(matricula)
            for (item in kardex) {
                database.sicenetDatabaseQueries.insertKardex(
                    matricula = matricula,
                    clave = item.clave,
                    nombre = item.nombre,
                    calificacion = item.calificacion,
                    acreditacion = item.acreditacion,
                    periodo = item.periodo,
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── Carga ──────────────────────────────────────────────────────────

    fun getCarga(matricula: String): List<MateriaCarga> {
        return database.sicenetDatabaseQueries.getCarga(matricula).executeAsList().map { entity ->
            MateriaCarga(
                nombre = entity.nombre,
                docente = entity.docente,
                grupo = entity.grupo,
                creditos = entity.creditos,
                lunes = entity.lunes,
                martes = entity.martes,
                miercoles = entity.miercoles,
                jueves = entity.jueves,
                viernes = entity.viernes,
                sabado = entity.sabado
            )
        }
    }

    fun saveCarga(matricula: String, carga: List<MateriaCarga>) {
        database.transaction {
            database.sicenetDatabaseQueries.deleteCarga(matricula)
            for (item in carga) {
                database.sicenetDatabaseQueries.insertCarga(
                    matricula = matricula,
                    nombre = item.nombre,
                    docente = item.docente,
                    grupo = item.grupo,
                    creditos = item.creditos,
                    lunes = item.lunes,
                    martes = item.martes,
                    miercoles = item.miercoles,
                    jueves = item.jueves,
                    viernes = item.viernes,
                    sabado = item.sabado,
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── CalifUnidad ────────────────────────────────────────────────────

    fun getCalifUnidad(matricula: String): List<MateriaParcial> {
        return database.sicenetDatabaseQueries.getCalifUnidad(matricula).executeAsList().map { entity ->
            MateriaParcial(
                materia = entity.materia,
                parciales = json.decodeFromString(entity.parciales)
            )
        }
    }

    fun saveCalifUnidad(matricula: String, parciales: List<MateriaParcial>) {
        database.transaction {
            database.sicenetDatabaseQueries.deleteCalifUnidad(matricula)
            for (item in parciales) {
                database.sicenetDatabaseQueries.insertCalifUnidad(
                    matricula = matricula,
                    materia = item.materia,
                    parciales = json.encodeToString(item.parciales),
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── CalifFinal ─────────────────────────────────────────────────────

    fun getCalifFinal(matricula: String): List<MateriaFinal> {
        return database.sicenetDatabaseQueries.getCalifFinal(matricula).executeAsList().map { entity ->
            MateriaFinal(
                materia = entity.materia,
                calif = entity.calif,
                acreditacion = entity.acreditacion,
                grupo = entity.grupo
            )
        }
    }

    fun saveCalifFinal(matricula: String, finales: List<MateriaFinal>) {
        database.transaction {
            database.sicenetDatabaseQueries.deleteCalifFinal(matricula)
            for (item in finales) {
                database.sicenetDatabaseQueries.insertCalifFinal(
                    matricula = matricula,
                    materia = item.materia,
                    calif = item.calif,
                    acreditacion = item.acreditacion,
                    grupo = item.grupo,
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── Utilities ──────────────────────────────────────────────────────

    fun clearAll(matricula: String) {
        database.transaction {
            database.sicenetDatabaseQueries.deleteProfile(matricula)
            database.sicenetDatabaseQueries.deleteKardex(matricula)
            database.sicenetDatabaseQueries.deleteCarga(matricula)
            database.sicenetDatabaseQueries.deleteCalifUnidad(matricula)
            database.sicenetDatabaseQueries.deleteCalifFinal(matricula)
        }
    }
}
