package com.ifpr.androidapptemplate.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.entity.Estatistica
import com.ifpr.androidapptemplate.data.entity.Sorteio
import com.ifpr.androidapptemplate.data.repository.LotofacilRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = LotofacilRepository(application)
    
    // LiveData para observação
    private val _ultimoSorteio = MutableLiveData<Sorteio?>()
    val ultimoSorteio: LiveData<Sorteio?> = _ultimoSorteio
    
    private val _estatisticas = MutableLiveData<List<Estatistica>>()
    val estatisticas: LiveData<List<Estatistica>> = _estatisticas
    
    private val _maisSorteados = MutableLiveData<List<Estatistica>>()
    val maisSorteados: LiveData<List<Estatistica>> = _maisSorteados
    
    private val _menosSorteados = MutableLiveData<List<Estatistica>>()
    val menosSorteados: LiveData<List<Estatistica>> = _menosSorteados
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _totalSorteios = MutableLiveData<Int>()
    val totalSorteios: LiveData<Int> = _totalSorteios
    
    init {
        carregarDados()
    }
    
    fun carregarDados() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Carregar último sorteio
                val sorteio = repository.getUltimoSorteio()
                _ultimoSorteio.value = sorteio
                
                // Carregar estatísticas
                repository.getAllEstatisticas().collect { estatisticas ->
                    _estatisticas.value = estatisticas
                    
                    // Separar mais e menos sorteados
                    val sorted = estatisticas.sortedByDescending { it.frequencia }
                    _maisSorteados.value = sorted.take(10)
                    _menosSorteados.value = sorted.takeLast(10).reversed()
                    
                    // Calcular total de sorteios
                    _totalSorteios.value = estatisticas.sumOf { it.frequencia }
                }
                
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sincronizarComPython() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                repository.sincronizarDadosPython()
                
                // Recarregar dados após sincronização
                carregarDados()
                
            } catch (e: Exception) {
                _error.value = "Erro na sincronização: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun webScrapingLotofacil() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val resultado = repository.webScrapingLotofacil()
                
                if (resultado["success"] == true) {
                    // Web scraping bem-sucedido
                    sincronizarComPython()
                } else {
                    _error.value = resultado["error"] as? String ?: "Erro desconhecido no web scraping"
                }
                
            } catch (e: Exception) {
                _error.value = "Erro no web scraping: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun limparErro() {
        _error.value = null
    }
    
    fun getProximoSorteio(): String {
        // Lógica para calcular próximo sorteio (geralmente às 20h de segunda a sábado)
        val calendar = Calendar.getInstance()
        val hoje = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Se for domingo (1), próximo sorteio é segunda (2)
        // Se for segunda a sábado (2-7), próximo sorteio é o próximo dia
        val proximoDia = when (hoje) {
            Calendar.SUNDAY -> Calendar.MONDAY
            Calendar.SATURDAY -> Calendar.MONDAY
            else -> hoje + 1
        }
        
        calendar.set(Calendar.DAY_OF_WEEK, proximoDia)
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        // Se o horário já passou hoje, ir para o próximo dia
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        return sdf.format(calendar.time)
    }
    
    fun getEstatisticasResumidas(): Map<String, Any> {
        val estatisticas = _estatisticas.value ?: emptyList()
        
        if (estatisticas.isEmpty()) {
            return mapOf(
                "total_numeros" to 0,
                "media_frequencia" to 0.0,
                "maior_frequencia" to 0,
                "menor_frequencia" to 0
            )
        }
        
        val totalNumeros = estatisticas.size
        val mediaFrequencia = estatisticas.map { it.frequencia }.average()
        val maiorFrequencia = estatisticas.maxOfOrNull { it.frequencia } ?: 0
        val menorFrequencia = estatisticas.minOfOrNull { it.frequencia } ?: 0
        
        return mapOf(
            "total_numeros" to totalNumeros,
            "media_frequencia" to mediaFrequencia,
            "maior_frequencia" to maiorFrequencia,
            "menor_frequencia" to menorFrequencia
        )
    }
}