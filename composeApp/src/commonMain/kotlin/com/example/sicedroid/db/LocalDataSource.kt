package com.example.sicedroid.db

import com.example.sicedroid.currentTimeMillis
import com.example.sicedroid.model.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class LocalDataSource(driverFactory: DriverFactory) {

    private val database = SicenetDatabase(driverFactory.createDriver())
    private val json = Json { ignoreUnknownKeys = true }
    private val stringListSerializer = ListSerializer(String.serializer())

    // ── Session ────────────────────────────────────────────────────────

    fun getSession(): SessionEntity? {
        return database.sessionQueries.getSession().executeAsOneOrNull()
    }

    fun sessionExists(): Boolean {
        return database.sessionQueries.sessionExists().executeAsOne() > 0
    }

    fun saveSession(matricula: String, password: String) {
        database.sessionQueries.saveSession(
            matricula = matricula,
            password = password,
            last_login = currentTimeMillis()
        )
    }

    fun clearSession() {
        database.sessionQueries.clearSession()
    }

    // ── Profile ────────────────────────────────────────────────────────

    fun getProfile(matricula: String): ProfileStudent? {
        val entity = database.profileQueries.getProfile(matricula).executeAsOneOrNull()
            ?: return null
        return ProfileStudent(
            matricula = entity.matricula,
            nombre = entity.nombre,
            apellidos = entity.apellidos,
            carrera = entity.carrera,
            especialidad = entity.especialidad,
            semestre = entity.semestre.toInt(),
            promedio = entity.promedio,
            estado = entity.estado,
            statusMatricula = entity.status_matricula,
            cdtsReunidos = entity.cdts_reunidos.toInt(),
            cdtsActuales = entity.cdts_actuales.toInt(),
            semActual = entity.sem_actual.toInt(),
            inscrito = entity.inscrito,
            reinscripcionFecha = entity.reinscripcion_fecha,
            estatusAlumno = entity.estatus_alumno,
            estatusAcademico = entity.estatus_academico,
            fotoUrl = entity.foto_url,
            sinAdeudos = entity.sin_adeudos,
            lineamiento = entity.lineamiento.toInt(),
            modEducativo = entity.mod_educativo.toInt(),
            operaciones = json.decodeFromString(stringListSerializer, entity.operaciones)
        )
    }

    fun saveProfile(matricula: String, profile: ProfileStudent) {
        database.profileQueries.insertProfile(
            matricula = matricula,
            nombre = profile.nombre,
            apellidos = profile.apellidos,
            carrera = profile.carrera,
            especialidad = profile.especialidad,
            semestre = profile.semestre.toLong(),
            promedio = profile.promedio,
            estado = profile.estado,
            status_matricula = profile.statusMatricula,
            cdts_reunidos = profile.cdtsReunidos.toLong(),
            cdts_actuales = profile.cdtsActuales.toLong(),
            sem_actual = profile.semActual.toLong(),
            inscrito = profile.inscrito,
            reinscripcion_fecha = profile.reinscripcionFecha,
            estatus_alumno = profile.estatusAlumno,
            estatus_academico = profile.estatusAcademico,
            foto_url = profile.fotoUrl,
            sin_adeudos = profile.sinAdeudos,
            lineamiento = profile.lineamiento.toLong(),
            mod_educativo = profile.modEducativo.toLong(),
            operaciones = json.encodeToString(stringListSerializer, profile.operaciones),
            last_update = currentTimeMillis()
        )
    }

    fun deleteProfile(matricula: String) {
        database.profileQueries.deleteProfile(matricula)
    }

    // ── Kardex ─────────────────────────────────────────────────────────

    fun getKardex(matricula: String): List<MateriaKardex> {
        return database.kardexQueries.getKardex(matricula).executeAsList().map { entity ->
            MateriaKardex(
                clave = entity.clave,
                nombre = entity.nombre,
                calificacion = entity.calificacion.toInt(),
                acreditacion = entity.acreditacion,
                periodo = entity.periodo
            )
        }
    }

    fun saveKardex(matricula: String, kardex: List<MateriaKardex>) {
        database.transaction {
            database.kardexQueries.deleteKardex(matricula)
            for (item in kardex) {
                database.kardexQueries.insertKardex(
                    matricula = matricula,
                    clave = item.clave,
                    nombre = item.nombre,
                    calificacion = item.calificacion.toLong(),
                    acreditacion = item.acreditacion,
                    periodo = item.periodo,
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── Carga ──────────────────────────────────────────────────────────

    fun getCarga(matricula: String): List<MateriaCarga> {
        return database.cargaQueries.getCarga(matricula).executeAsList().map { entity ->
            MateriaCarga(
                nombre = entity.nombre,
                docente = entity.docente,
                grupo = entity.grupo,
                creditos = entity.creditos.toInt(),
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
            database.cargaQueries.deleteCarga(matricula)
            for (item in carga) {
                database.cargaQueries.insertCarga(
                    matricula = matricula,
                    nombre = item.nombre,
                    docente = item.docente,
                    grupo = item.grupo,
                    creditos = item.creditos.toLong(),
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
        return database.califUnidadQueries.getCalifUnidad(matricula).executeAsList().map { entity ->
            MateriaParcial(
                materia = entity.materia,
                parciales = json.decodeFromString(stringListSerializer, entity.parciales)
            )
        }
    }

    fun saveCalifUnidad(matricula: String, parciales: List<MateriaParcial>) {
        database.transaction {
            database.califUnidadQueries.deleteCalifUnidad(matricula)
            for (item in parciales) {
                database.califUnidadQueries.insertCalifUnidad(
                    matricula = matricula,
                    materia = item.materia,
                    parciales = json.encodeToString(stringListSerializer, item.parciales),
                    last_update = currentTimeMillis()
                )
            }
        }
    }

    // ── CalifFinal ─────────────────────────────────────────────────────

    fun getCalifFinal(matricula: String): List<MateriaFinal> {
        return database.califFinalQueries.getCalifFinal(matricula).executeAsList().map { entity ->
            MateriaFinal(
                materia = entity.materia,
                calif = entity.calif.toInt(),
                acreditacion = entity.acreditacion,
                grupo = entity.grupo
            )
        }
    }

    fun saveCalifFinal(matricula: String, finales: List<MateriaFinal>) {
        database.transaction {
            database.califFinalQueries.deleteCalifFinal(matricula)
            for (item in finales) {
                database.califFinalQueries.insertCalifFinal(
                    matricula = matricula,
                    materia = item.materia,
                    calif = item.calif.toLong(),
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
            database.profileQueries.deleteProfile(matricula)
            database.kardexQueries.deleteKardex(matricula)
            database.cargaQueries.deleteCarga(matricula)
            database.califUnidadQueries.deleteCalifUnidad(matricula)
            database.califFinalQueries.deleteCalifFinal(matricula)
        }
    }
}
