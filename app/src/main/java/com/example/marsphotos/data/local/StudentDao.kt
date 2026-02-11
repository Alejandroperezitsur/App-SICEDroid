package com.example.marsphotos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM student_table LIMIT 1")
    fun getStudent(): Flow<StudentEntity?>

    @Query("SELECT * FROM student_table WHERE matricula = :matricula")
    suspend fun getStudentSync(matricula: String): StudentEntity?

    // Kardex
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKardex(kardex: List<KardexEntity>)

    @Query("DELETE FROM kardex_table WHERE matricula = :matricula")
    suspend fun deleteKardex(matricula: String)

    @Query("SELECT * FROM kardex_table WHERE matricula = :matricula")
    fun getKardex(matricula: String): Flow<List<KardexEntity>>

    @Query("SELECT * FROM kardex_table WHERE matricula = :matricula")
    suspend fun getKardexSync(matricula: String): List<KardexEntity>

    // Carga
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarga(carga: List<CargaEntity>)

    @Query("DELETE FROM carga_table WHERE matricula = :matricula")
    suspend fun deleteCarga(matricula: String)

    @Query("SELECT * FROM carga_table WHERE matricula = :matricula")
    fun getCarga(matricula: String): Flow<List<CargaEntity>>

    @Query("SELECT * FROM carga_table WHERE matricula = :matricula")
    suspend fun getCargaSync(matricula: String): List<CargaEntity>

    // Calif Unidad
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalifUnidad(calif: List<CalifUnidadEntity>)

    @Query("DELETE FROM calif_unidad_table WHERE matricula = :matricula")
    suspend fun deleteCalifUnidad(matricula: String)

    @Query("SELECT * FROM calif_unidad_table WHERE matricula = :matricula")
    fun getCalifUnidad(matricula: String): Flow<List<CalifUnidadEntity>>

    @Query("SELECT * FROM calif_unidad_table WHERE matricula = :matricula")
    suspend fun getCalifUnidadSync(matricula: String): List<CalifUnidadEntity>

    // Calif Final
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalifFinal(calif: List<CalifFinalEntity>)

    @Query("DELETE FROM calif_final_table WHERE matricula = :matricula")
    suspend fun deleteCalifFinal(matricula: String)

    @Query("SELECT * FROM calif_final_table WHERE matricula = :matricula")
    fun getCalifFinal(matricula: String): Flow<List<CalifFinalEntity>>

    @Query("SELECT * FROM calif_final_table WHERE matricula = :matricula")
    suspend fun getCalifFinalSync(matricula: String): List<CalifFinalEntity>
}
