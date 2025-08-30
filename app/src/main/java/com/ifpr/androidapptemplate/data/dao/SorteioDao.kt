package com.ifpr.androidapptemplate.data.dao

import androidx.room.*
import com.ifpr.androidapptemplate.data.entity.Sorteio
import kotlinx.coroutines.flow.Flow

@Dao
interface SorteioDao {
    
    @Query("SELECT * FROM sorteios ORDER BY concurso DESC")
    fun getAllSorteios(): Flow<List<Sorteio>>
    
    @Query("SELECT * FROM sorteios ORDER BY concurso DESC LIMIT :limite")
    fun getSorteiosLimit(limite: Int): Flow<List<Sorteio>>
    
    @Query("SELECT * FROM sorteios WHERE concurso = :concurso")
    suspend fun getSorteioByConcurso(concurso: Int): Sorteio?
    
    @Query("SELECT * FROM sorteios ORDER BY concurso DESC LIMIT 1")
    suspend fun getUltimoSorteio(): Sorteio?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSorteio(sorteio: Sorteio)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSorteios(sorteios: List<Sorteio>)
    
    @Update
    suspend fun updateSorteio(sorteio: Sorteio)
    
    @Delete
    suspend fun deleteSorteio(sorteio: Sorteio)
    
    @Query("DELETE FROM sorteios")
    suspend fun deleteAllSorteios()
    
    @Query("SELECT COUNT(*) FROM sorteios")
    suspend fun getTotalSorteios(): Int
}
