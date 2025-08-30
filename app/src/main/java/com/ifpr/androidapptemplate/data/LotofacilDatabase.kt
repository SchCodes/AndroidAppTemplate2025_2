package com.ifpr.androidapptemplate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ifpr.androidapptemplate.data.converter.ListConverter
import com.ifpr.androidapptemplate.data.dao.EstatisticaDao
import com.ifpr.androidapptemplate.data.dao.SorteioDao
import com.ifpr.androidapptemplate.data.dao.UsuarioDao
import com.ifpr.androidapptemplate.data.entity.Estatistica
import com.ifpr.androidapptemplate.data.entity.Sorteio
import com.ifpr.androidapptemplate.data.entity.Usuario

@Database(
    entities = [Sorteio::class, Estatistica::class, Usuario::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class LotofacilDatabase : RoomDatabase() {
    
    abstract fun sorteioDao(): SorteioDao
    abstract fun estatisticaDao(): EstatisticaDao
    abstract fun usuarioDao(): UsuarioDao
    
    companion object {
        @Volatile
        private var INSTANCE: LotofacilDatabase? = null
        
        fun getDatabase(context: Context): LotofacilDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotofacilDatabase::class.java,
                    "lotofacil_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
