package com.ifpr.androidapptemplate.data.repository

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.ifpr.androidapptemplate.data.LotofacilDatabase
import com.ifpr.androidapptemplate.data.dao.EstatisticaDao
import com.ifpr.androidapptemplate.data.dao.SorteioDao
import com.ifpr.androidapptemplate.data.dao.UsuarioDao
import com.ifpr.androidapptemplate.data.entity.Estatistica
import com.ifpr.androidapptemplate.data.entity.Sorteio
import com.ifpr.androidapptemplate.data.entity.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LotofacilRepository(context: Context) {
    
    private val database = LotofacilDatabase.getDatabase(context)
    private val sorteioDao: SorteioDao = database.sorteioDao()
    private val estatisticaDao: EstatisticaDao = database.estatisticaDao()
    private val usuarioDao: UsuarioDao = database.usuarioDao()
    
    private val python: Python
    private val lotofacilModule: com.chaquo.python.PyObject
    
    init {
        // Inicializar Python se ainda não foi inicializado
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        python = Python.getInstance()
        lotofacilModule = python.getModule("lotofacil_backend")
    }
    
    // ===== OPERAÇÕES COM SORTEIOS =====
    
    fun getAllSorteios(): Flow<List<Sorteio>> = sorteioDao.getAllSorteios()
    
    fun getSorteiosLimit(limite: Int): Flow<List<Sorteio>> = sorteioDao.getSorteiosLimit(limite)
    
    suspend fun getUltimoSorteio(): Sorteio? = withContext(Dispatchers.IO) {
        sorteioDao.getUltimoSorteio()
    }
    
    suspend fun insertSorteio(sorteio: Sorteio) = withContext(Dispatchers.IO) {
        sorteioDao.insertSorteio(sorteio)
    }
    
    suspend fun insertSorteios(sorteios: List<Sorteio>) = withContext(Dispatchers.IO) {
        sorteioDao.insertSorteios(sorteios)
    }
    
    // ===== OPERAÇÕES COM ESTATÍSTICAS =====
    
    fun getAllEstatisticas(): Flow<List<Estatistica>> = estatisticaDao.getAllEstatisticas()
    
    fun getEstatisticasLimit(limite: Int): Flow<List<Estatistica>> = estatisticaDao.getEstatisticasLimit(limite)
    
    fun getEstatisticasMenosSorteadas(limite: Int): Flow<List<Estatistica>> = 
        estatisticaDao.getEstatisticasMenosSorteadas(limite)
    
    suspend fun insertEstatistica(estatistica: Estatistica) = withContext(Dispatchers.IO) {
        estatisticaDao.insertEstatistica(estatistica)
    }
    
    suspend fun insertEstatisticas(estatisticas: List<Estatistica>) = withContext(Dispatchers.IO) {
        estatisticaDao.insertEstatisticas(estatisticas)
    }
    
    // ===== OPERAÇÕES COM USUÁRIOS =====
    
    suspend fun getUsuarioByEmail(email: String): Usuario? = withContext(Dispatchers.IO) {
        usuarioDao.getUsuarioByEmail(email)
    }
    
    suspend fun insertUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        usuarioDao.insertUsuario(usuario)
    }
    
    suspend fun verificarAdmin(email: String): Boolean = withContext(Dispatchers.IO) {
        val usuario = usuarioDao.getUsuarioByEmail(email)
        usuario?.isAdmin ?: false
    }
    
    // ===== OPERAÇÕES PYTHON =====
    
    suspend fun webScrapingLotofacil(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val resultado = lotofacilModule.callAttr("web_scraping_lotofacil")
            if (resulto != null) {
                val success = resultado.get("success").toBoolean()
                if (success) {
                    val numeros = resultado.get("numeros").asList().map { it.toInt() }
                    val dataAtualizacao = resultado.get("data_atualizacao").toString()
                    
                    mapOf(
                        "success" to true,
                        "numeros" to numeros,
                        "data_atualizacao" to dataAtualizacao
                    )
                } else {
                    mapOf(
                        "success" to false,
                        "error" to resultado.get("error").toString()
                    )
                }
            } else {
                mapOf("success" to false, "error" to "Erro desconhecido")
            }
        } catch (e: Exception) {
            mapOf("success" to false, "error" to e.message ?: "Erro desconhecido")
        }
    }
    
    suspend fun obterUltimoResultadoPython(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val resultado = lotofacilModule.callAttr("obter_ultimo_resultado")
            if (resulto != null) {
                val success = resultado.get("success").toBoolean()
                if (success) {
                    val concurso = resultado.get("concurso").toInt()
                    val dataSorteio = resultado.get("data_sorteio").toString()
                    val numeros = resultado.get("numeros").asList().map { it.toInt() }
                    val dataAtualizacao = resultado.get("data_atualizacao").toString()
                    
                    mapOf(
                        "success" to true,
                        "concurso" to concurso,
                        "data_sorteio" to dataSorteio,
                        "numeros" to numeros,
                        "data_atualizacao" to dataAtualizacao
                    )
                } else {
                    mapOf(
                        "success" to false,
                        "error" to resultado.get("error").toString()
                    )
                }
            } else {
                mapOf("success" to false, "error" to "Erro desconhecido")
            }
        } catch (e: Exception) {
            mapOf("success" to false, "error" to e.message ?: "Erro desconhecido")
        }
    }
    
    suspend fun obterEstatisticasPython(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val resultado = lotofacilModule.callAttr("obter_estatisticas")
            if (resulto != null) {
                val success = resultado.get("success").toBoolean()
                if (success) {
                    val totalSorteios = resultado.get("total_sorteios").toInt()
                    val maisSorteados = resultado.get("mais_sorteados").asList().map { 
                        mapOf(
                            "numero" to it.get("numero").toInt(),
                            "frequencia" to it.get("frequencia").toInt()
                        )
                    }
                    val menosSorteados = resultado.get("menos_sorteados").asList().map { 
                        mapOf(
                            "numero" to it.get("numero").toInt(),
                            "frequencia" to it.get("frequencia").toInt()
                        )
                    }
                    
                    mapOf(
                        "success" to true,
                        "total_sorteios" to totalSorteios,
                        "mais_sorteados" to maisSorteados,
                        "menos_sorteados" to menosSorteados
                    )
                } else {
                    mapOf(
                        "success" to false,
                        "error" to resultado.get("error").toString()
                    )
                }
            } else {
                mapOf("success" to false, "error" to "Erro desconhecido")
            }
        } catch (e: Exception) {
            mapOf("success" to false, "error" to e.message ?: "Erro desconhecido")
        }
    }
    
    // ===== UTILITÁRIOS =====
    
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    
    suspend fun sincronizarDadosPython() = withContext(Dispatchers.IO) {
        try {
            // Obter último resultado do Python
            val resultadoPython = obterUltimoResultadoPython()
            
            if (resultadoPython["success"] == true) {
                val concurso = resultadoPython["concurso"] as Int
                val dataSorteio = resultadoPython["data_sorteio"] as String
                val numeros = resultadoPython["numeros"] as List<Int>
                val dataAtualizacao = resultadoPython["data_atualizacao"] as String
                
                // Verificar se já existe no banco local
                val sorteioExistente = sorteioDao.getSorteioByConcurso(concurso)
                
                if (sorteioExistente == null) {
                    // Inserir novo sorteio
                    val novoSorteio = Sorteio(
                        concurso = concurso,
                        dataSorteio = dataSorteio,
                        numeros = numeros,
                        dataAtualizacao = dataAtualizacao
                    )
                    sorteioDao.insertSorteio(novoSorteio)
                    
                    // Atualizar estatísticas
                    atualizarEstatisticas(numeros)
                }
            }
        } catch (e: Exception) {
            // Log do erro
            e.printStackTrace()
        }
    }
    
    private suspend fun atualizarEstatisticas(numeros: List<Int>) {
        for (numero in numeros) {
            val estatisticaExistente = estatisticaDao.getEstatisticaByNumero(numero)
            
            if (estatisticaExistente != null) {
                // Atualizar frequência
                val estatisticaAtualizada = estatisticaExistente.copy(
                    frequencia = estatisticaExistente.frequencia + 1,
                    ultimaAtualizacao = getCurrentDateTime()
                )
                estatisticaDao.updateEstatistica(estatisticaAtualizada)
            } else {
                // Criar nova estatística
                val novaEstatistica = Estatistica(
                    numero = numero,
                    frequencia = 1,
                    ultimaAtualizacao = getCurrentDateTime()
                )
                estatisticaDao.insertEstatistica(novaEstatistica)
            }
        }
    }
}
