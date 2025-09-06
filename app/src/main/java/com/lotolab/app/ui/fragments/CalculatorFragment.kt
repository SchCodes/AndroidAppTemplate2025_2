package com.lotolab.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.lotolab.app.R
import com.lotolab.app.databinding.FragmentCalculatorBinding
import com.lotolab.app.ui.viewmodels.CalculatorViewModel
import org.json.JSONObject

/**
 * CalculatorFragment - Calculadora de probabilidades da Lotofácil
 * Integra com Python via Chaquopy para cálculos locais
 */
class CalculatorFragment : Fragment() {
    
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CalculatorViewModel
    private var numerosSelecionados = mutableSetOf<Int>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicialização do ViewModel
        viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]
        
        // Inicialização do Python
        initializePython()
        
        // Configuração dos listeners
        setupListeners()
        
        // Observação dos dados
        observeData()
        
        // Configuração inicial
        setupInitialState()
    }
    
    private fun initializePython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }
    }
    
    private fun setupListeners() {
        // Botões de números (1-25)
        setupNumberButtons()
        
        // Botões de ação
        binding.btnCalcular.setOnClickListener {
            if (numerosSelecionados.size == 15) {
                executarCalculo()
            } else {
                Toast.makeText(context, "Selecione exatamente 15 números", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnLimpar.setOnClickListener {
            limparSelecao()
        }
        
        binding.btnSugestao.setOnClickListener {
            gerarSugestao()
        }
        
        // Spinner de tipo de cálculo
        binding.spinnerTipoCalculo.setOnItemSelectedListener { _, _, position, _ ->
            // Atualiza tipo de cálculo selecionado
            viewModel.setTipoCalculo(position)
        }
    }
    
    private fun setupNumberButtons() {
        // Cria botões para números 1-25
        for (i in 1..25) {
            val buttonId = resources.getIdentifier("btn_$i", "id", requireContext().packageName)
            val button = binding.root.findViewById<android.widget.Button>(buttonId)
            
            button?.setOnClickListener {
                toggleNumero(i)
            }
        }
    }
    
    private fun toggleNumero(numero: Int) {
        if (numerosSelecionados.contains(numero)) {
            numerosSelecionados.remove(numero)
            updateButtonState(numero, false)
        } else if (numerosSelecionados.size < 15) {
            numerosSelecionados.add(numero)
            updateButtonState(numero, true)
        } else {
            Toast.makeText(context, "Máximo de 15 números permitido", Toast.LENGTH_SHORT).show()
        }
        
        updateContador()
        updateBotaoCalcular()
    }
    
    private fun updateButtonState(numero: Int, selecionado: Boolean) {
        val buttonId = resources.getIdentifier("btn_$numero", "id", requireContext().packageName)
        val button = binding.root.findViewById<android.widget.Button>(buttonId)
        
        button?.apply {
            if (selecionado) {
                setBackgroundColor(resources.getColor(R.color.primary, null))
                setTextColor(resources.getColor(R.color.white, null))
            } else {
                setBackgroundColor(resources.getColor(R.color.surface, null))
                setTextColor(resources.getColor(R.color.on_surface, null))
            }
        }
    }
    
    private fun updateContador() {
        binding.tvContador.text = "${numerosSelecionados.size}/15"
        
        // Atualiza cor do contador
        val cor = when {
            numerosSelecionados.size == 15 -> R.color.success
            numerosSelecionados.size > 10 -> R.color.warning
            else -> R.color.error
        }
        binding.tvContador.setTextColor(resources.getColor(cor, null))
    }
    
    private fun updateBotaoCalcular() {
        binding.btnCalcular.isEnabled = numerosSelecionados.size == 15
    }
    
    private fun limparSelecao() {
        numerosSelecionados.clear()
        
        // Reseta todos os botões
        for (i in 1..25) {
            updateButtonState(i, false)
        }
        
        updateContador()
        updateBotaoCalcular()
        
        // Limpa resultados
        binding.tvResultado.text = ""
        binding.cardResultado.visibility = View.GONE
    }
    
    private fun executarCalculo() {
        if (numerosSelecionados.size != 15) {
            Toast.makeText(context, "Selecione exatamente 15 números", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Verifica se usuário pode calcular
        viewModel.verificarPermissaoCalculo { podeCalcular ->
            if (podeCalcular) {
                executarCalculoPython()
            } else {
                showPremiumDialog()
            }
        }
    }
    
    private fun executarCalculoPython() {
        try {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnCalcular.isEnabled = false
            
            // Prepara dados para Python
            val dados = JSONObject().apply {
                put("numeros", numerosSelecionados.toList().sorted())
            }
            
            // Executa cálculo via Python
            val python = Python.getInstance()
            val module = python.getModule("probabilidades")
            
            val tipoCalculo = when (binding.spinnerTipoCalculo.selectedItemPosition) {
                0 -> "probabilidade_simples"
                1 -> "frequencia_numeros"
                2 -> "padroes"
                else -> "probabilidade_simples"
            }
            
            val resultado = module.callAttr("executar_calculo", tipoCalculo, dados.toString())
            
            // Processa resultado
            processarResultado(resultado.toString())
            
            // Registra cálculo no histórico
            viewModel.registrarCalculo(tipoCalculo, numerosSelecionados.toList(), resultado.toString())
            
        } catch (e: Exception) {
            showError("Erro ao executar cálculo: ${e.message}")
        } finally {
            binding.progressBar.visibility = View.GONE
            binding.btnCalcular.isEnabled = true
        }
    }
    
    private fun processarResultado(resultadoJson: String) {
        try {
            val resultado = JSONObject(resultadoJson)
            
            if (resultado.has("erro")) {
                showError(resultado.getString("erro"))
                return
            }
            
            // Exibe resultado
            binding.cardResultado.visibility = View.VISIBLE
            
            when (binding.spinnerTipoCalculo.selectedItemPosition) {
                0 -> exibirProbabilidades(resultado)
                1 -> exibirFrequencias(resultado)
                2 -> exibirPadroes(resultado)
                else -> exibirProbabilidades(resultado)
            }
            
        } catch (e: Exception) {
            showError("Erro ao processar resultado: ${e.message}")
        }
    }
    
    private fun exibirProbabilidades(resultado: JSONObject) {
        binding.apply {
            tvTituloResultado.text = "Probabilidades de Acerto"
            
            val numeros = resultado.getJSONArray("numeros")
            val probabilidades = resultado.getJSONObject("probabilidades")
            
            var textoResultado = "Números analisados: ${numeros.joinToString(" - ")}\n\n"
            
            // Exibe probabilidades para cada quantidade de acertos
            for (i in 0 until probabilidades.length()) {
                val key = probabilidades.names().getString(i)
                val prob = probabilidades.getJSONObject(key)
                
                val acertos = key.replace("_acertos", "")
                val percentual = prob.getDouble("percentual")
                val chance = prob.getString("chance_em_1")
                
                textoResultado += "$acertos acertos: ${String.format("%.4f", percentual)}% (1 em $chance)\n"
            }
            
            tvResultado.text = textoResultado
        }
    }
    
    private fun exibirFrequencias(resultado: JSONObject) {
        binding.apply {
            tvTituloResultado.text = "Análise de Frequência"
            
            val totalConcursos = resultado.getInt("total_concursos")
            val maisSorteados = resultado.getJSONArray("mais_sorteados")
            
            var textoResultado = "Total de concursos analisados: $totalConcursos\n\n"
            textoResultado += "Números mais sorteados:\n"
            
            for (i in 0 until maisSorteados.length()) {
                val item = maisSorteados.getJSONObject(i)
                val numero = item.getInt("numero")
                val frequencia = item.getInt("frequencia")
                textoResultado += "$numero: $frequencia vezes\n"
            }
            
            tvResultado.text = textoResultado
        }
    }
    
    private fun exibirPadroes(resultado: JSONObject) {
        binding.apply {
            tvTituloResultado.text = "Análise de Padrões"
            
            val padroes = resultado.getJSONObject("padroes_analisados")
            
            var textoResultado = "Padrões encontrados:\n\n"
            
            // Pares vs Ímpares
            val paresImpares = padroes.getJSONObject("pares_impares")
            val mediaPares = paresImpares.getDouble("media_pares")
            val mediaImpares = paresImpares.getDouble("media_impares")
            textoResultado += "Média de pares: ${String.format("%.1f", mediaPares)}\n"
            textoResultado += "Média de ímpares: ${String.format("%.1f", mediaImpares)}\n\n"
            
            // Baixos vs Altos
            val baixosAltos = padroes.getJSONObject("baixos_altos")
            val mediaBaixos = baixosAltos.getDouble("media_baixos")
            val mediaAltos = baixosAltos.getDouble("media_altos")
            textoResultado += "Média de baixos (1-12): ${String.format("%.1f", mediaBaixos)}\n"
            textoResultado += "Média de altos (13-25): ${String.format("%.1f", mediaAltos)}\n\n"
            
            // Soma total
            val mediaSoma = padroes.getDouble("soma_total")
            textoResultado += "Média da soma total: ${String.format("%.1f", mediaSoma)}"
            
            tvResultado.text = textoResultado
        }
    }
    
    private fun gerarSugestao() {
        // TODO: Implementar geração de sugestão baseada em dados históricos
        Toast.makeText(context, "Geração de sugestão em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPremiumDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Limite de Cálculos Atingido")
            .setMessage("Você atingiu o limite de 3 cálculos por dia. Faça upgrade para premium e tenha cálculos ilimitados!")
            .setPositiveButton("Ver Premium") { _, _ ->
                // TODO: Navegar para tela de premium
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showError(message: String) {
        binding.cardResultado.visibility = View.VISIBLE
        binding.tvTituloResultado.text = "Erro"
        binding.tvResultado.text = message
        binding.tvResultado.setTextColor(resources.getColor(R.color.error, null))
    }
    
    private fun observeData() {
        // Observa permissão de cálculo
        viewModel.podeCalcular.observe(viewLifecycleOwner) { pode ->
            binding.btnCalcular.isEnabled = pode && numerosSelecionados.size == 15
        }
        
        // Observa erros
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }
    }
    
    private fun setupInitialState() {
        updateContador()
        updateBotaoCalcular()
        binding.cardResultado.visibility = View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
