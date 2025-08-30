package com.ifpr.androidapptemplate.data.dao

import androidx.room.*
import com.ifpr.androidapptemplate.data.entity.Estatistica
import kotlinx.coroutines.flow.Flow

@Dao
interface EstatisticaDao {
    
    @Query("SELECT * FROM estatisticas ORDER BY frequencia DESC")
    fun getAllEstatisticas(): Flow<List<Estatistica>>
    
    @Query("SELECT * FROM estatisticas WHERE numero = :numero")
    suspend fun getEstatisticaByNumero(numero: Int): Estatistica?
    
    @Query("SELECT * FROM estatisticas ORDER BY frequencia DESC LIMIT :limite")
    fun getEstatisticasLimit(limite: Int): Flow<List<Estatistica>>
    
    @Query("SELECT * FROM estatisticas ORDER BY frequencia ASC LIMIT :limite")
    fun getEstatisticasMenosSorteadas(limite: Int): Flow<List<Estatistica>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstatistica(estatistica: Estatistica)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstatisticas(estatisticas: List<Estatistica>)
    
    @Update
    suspend fun updateEstatistica(estatistica: Estatistica)
    
    @Delete
    suspend fun deleteEstatistica(estatistica: Estatistica)
    
    @Query("DELETE FROM estatisticas")
    suspend fun deleteAllEstatisticas()
    
    @Query("SELECT SUM(frequencia) FROM estatisticas")
    suspend fun getTotalFrequencias(): Int?
}
