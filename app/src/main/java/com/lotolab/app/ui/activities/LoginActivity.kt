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
import com.lotolab.app.databinding.ActivityLoginBinding

/**
 * LoginActivity para autenticação do usuário
 * Suporta login com email/senha e Google
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuração do tema
        setTheme(R.style.Theme_LotoLab)
        
        // ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicialização do Firebase
        setupFirebase()
        
        // Configuração dos listeners
        setupListeners()
        
        // Verifica se usuário já está logado
        checkCurrentUser()
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
        // Login com email/senha
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            
            if (validateInputs(email, password)) {
                signInWithEmailAndPassword(email, password)
            }
        }
        
        // Login com Google
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        // Ir para registro
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        // Esqueci a senha
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implementar recuperação de senha
            Toast.makeText(this, "Recuperação de senha em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email é obrigatório"
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            return false
        }
        
        if (password.isEmpty()) {
            binding.etPassword.error = "Senha é obrigatória"
            return false
        }
        
        if (password.length < 6) {
            binding.etPassword.error = "Senha deve ter pelo menos 6 caracteres"
            return false
        }
        
        return true
    }
    
    private fun signInWithEmailAndPassword(email: String, password: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnLogin.isEnabled = false
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnLogin.isEnabled = true
                
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    val user = auth.currentUser
                    if (user != null) {
                        // Verifica se email foi verificado
                        if (user.isEmailVerified) {
                            navigateToMain()
                        } else {
                            showEmailVerificationDialog()
                        }
                    }
                } else {
                    // Login falhou
                    val errorMessage = task.exception?.message ?: "Erro no login"
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
                Toast.makeText(this, "Login com Google falhou: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = android.view.View.GONE
                
                if (task.isSuccessful) {
                    // Login com Google bem-sucedido
                    navigateToMain()
                } else {
                    // Login com Google falhou
                    val errorMessage = task.exception?.message ?: "Erro no login com Google"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuário já está logado, vai para MainActivity
            navigateToMain()
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
            .setMessage("Por favor, verifique seu email antes de continuar.")
            .setPositiveButton("Reenviar") { _, _ ->
                auth.currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email de verificação reenviado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro ao reenviar email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("OK", null)
            .show()
    }
    
    override fun onBackPressed() {
        // Pergunta se quer sair do app
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sair do LotoLab")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _, _ ->
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
