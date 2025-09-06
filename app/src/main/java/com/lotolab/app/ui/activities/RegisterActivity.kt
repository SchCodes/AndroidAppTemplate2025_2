package com.lotolab.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.lotolab.app.R
import com.lotolab.app.databinding.ActivityRegisterBinding

/**
 * RegisterActivity para cadastro de novos usuários
 * Suporta cadastro com email/senha e Google
 */
class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    
    companion object {
        private const val RC_SIGN_IN = 9002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuração do tema
        setTheme(R.style.Theme_LotoLab)
        
        // ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Configuração da toolbar
        setupToolbar()
        
        // Inicialização do Firebase
        setupFirebase()
        
        // Configuração dos listeners
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
        
        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupListeners() {
        // Cadastro com email/senha
        binding.btnCadastrar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()
            val confirmarSenha = binding.etConfirmarSenha.text.toString()
            
            if (validateInputs(nome, email, senha, confirmarSenha)) {
                createUserWithEmailAndPassword(nome, email, senha)
            }
        }
        
        // Cadastro com Google
        binding.btnGoogleSignup.setOnClickListener {
            signInWithGoogle()
        }
        
        // Ir para login
        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }
    
    private fun validateInputs(nome: String, email: String, senha: String, confirmarSenha: String): Boolean {
        var isValid = true
        
        // Validação do nome
        if (nome.isEmpty()) {
            binding.etNome.error = "Nome é obrigatório"
            isValid = false
        } else if (nome.length < 2) {
            binding.etNome.error = "Nome deve ter pelo menos 2 caracteres"
            isValid = false
        }
        
        // Validação do email
        if (email.isEmpty()) {
            binding.etEmail.error = "Email é obrigatório"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            isValid = false
        }
        
        // Validação da senha
        if (senha.isEmpty()) {
            binding.etSenha.error = "Senha é obrigatória"
            isValid = false
        } else if (senha.length < 6) {
            binding.etSenha.error = "Senha deve ter pelo menos 6 caracteres"
            isValid = false
        }
        
        // Validação da confirmação de senha
        if (confirmarSenha.isEmpty()) {
            binding.etConfirmarSenha.error = "Confirmação de senha é obrigatória"
            isValid = false
        } else if (senha != confirmarSenha) {
            binding.etConfirmarSenha.error = "Senhas não coincidem"
            isValid = false
        }
        
        // Validação dos termos
        if (!binding.cbTermos.isChecked) {
            Toast.makeText(this, "Você deve aceitar os termos de uso", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
    
    private fun createUserWithEmailAndPassword(nome: String, email: String, senha: String) {
        showProgress(true)
        
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Cadastro bem-sucedido
                    val user = auth.currentUser
                    if (user != null) {
                        // Atualiza o perfil do usuário com o nome
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(nome)
                            .build()
                        
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Envia email de verificação
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            showProgress(false)
                                            if (emailTask.isSuccessful) {
                                                showEmailVerificationDialog()
                                            } else {
                                                Toast.makeText(this, "Erro ao enviar email de verificação", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    showProgress(false)
                                    Toast.makeText(this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Cadastro falhou
                    showProgress(false)
                    val errorMessage = task.exception?.message ?: "Erro no cadastro"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Cadastro com Google falhou: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        showProgress(true)
        
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showProgress(false)
                
                if (task.isSuccessful) {
                    // Cadastro com Google bem-sucedido
                    navigateToMain()
                } else {
                    // Cadastro com Google falhou
                    val errorMessage = task.exception?.message ?: "Erro no cadastro com Google"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showEmailVerificationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Verificar Email")
            .setMessage("Enviamos um email de verificação para ${auth.currentUser?.email}. " +
                    "Por favor, verifique sua caixa de entrada e clique no link para ativar sua conta.")
            .setPositiveButton("OK") { _, _ ->
                // Volta para a tela de login
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnCadastrar.isEnabled = !show
        binding.btnGoogleSignup.isEnabled = !show
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
