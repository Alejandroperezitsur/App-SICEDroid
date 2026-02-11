package com.example.marsphotos.data.local

import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    suspend fun insertStudent(student: StudentEntity)
    fun getStudent(): Flow<StudentEntity?>
    suspend fun getStudentSync(matricula: String): StudentEntity?

    suspend fun insertKardex(kardex: List<KardexEntity>)
    fun getKardex(matricula: String): Flow<List<KardexEntity>>
    suspend fun getKardexSync(matricula: String): List<KardexEntity>

    suspend fun insertCarga(carga: List<CargaEntity>)
    fun getCarga(matricula: String): Flow<List<CargaEntity>>
    suspend fun getCargaSync(matricula: String): List<CargaEntity>

    suspend fun insertCalifUnidad(calif: List<CalifUnidadEntity>)
    fun getCalifUnidad(matricula: String): Flow<List<CalifUnidadEntity>>
    suspend fun getCalifUnidadSync(matricula: String): List<CalifUnidadEntity>

    suspend fun insertCalifFinal(calif: List<CalifFinalEntity>)
    fun getCalifFinal(matricula: String): Flow<List<CalifFinalEntity>>
    suspend fun getCalifFinalSync(matricula: String): List<CalifFinalEntity>
}

class LocalRepositoryImpl(private val studentDao: StudentDao) : LocalRepository {
    override suspend fun insertStudent(student: StudentEntity) = studentDao.insertStudent(student)
    override fun getStudent(): Flow<StudentEntity?> = studentDao.getStudent()
    override suspend fun getStudentSync(matricula: String): StudentEntity? = studentDao.getStudentSync(matricula)

    override suspend fun insertKardex(kardex: List<KardexEntity>) = studentDao.insertKardex(kardex)
    override fun getKardex(matricula: String): Flow<List<KardexEntity>> = studentDao.getKardex(matricula)
    override suspend fun getKardexSync(matricula: String): List<KardexEntity> = studentDao.getKardexSync(matricula)

    override suspend fun insertCarga(carga: List<CargaEntity>) = studentDao.insertCarga(carga)
    override fun getCarga(matricula: String): Flow<List<CargaEntity>> = studentDao.getCarga(matricula)
    override suspend fun getCargaSync(matricula: String): List<CargaEntity> = studentDao.getCargaSync(matricula)

    override suspend fun insertCalifUnidad(calif: List<CalifUnidadEntity>) = studentDao.insertCalifUnidad(calif)
    override fun getCalifUnidad(matricula: String): Flow<List<CalifUnidadEntity>> = studentDao.getCalifUnidad(matricula)
    override suspend fun getCalifUnidadSync(matricula: String): List<CalifUnidadEntity> = studentDao.getCalifUnidadSync(matricula)

    override suspend fun insertCalifFinal(calif: List<CalifFinalEntity>) = studentDao.insertCalifFinal(calif)
    override fun getCalifFinal(matricula: String): Flow<List<CalifFinalEntity>> = studentDao.getCalifFinal(matricula)
    override suspend fun getCalifFinalSync(matricula: String): List<CalifFinalEntity> = studentDao.getCalifFinalSync(matricula)
}
