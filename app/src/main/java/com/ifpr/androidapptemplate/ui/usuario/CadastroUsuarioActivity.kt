package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario

class CadastroUsuarioActivity : AppCompatActivity() {
    private lateinit var textCadastroUsuarioTitle: TextView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        textCadastroUsuarioTitle = findViewById(R.id.textCadastroUsuarioTitle)
        registerNameEditText = findViewById(R.id.registerNameEditText)
        registerEmailEditText = findViewById(R.id.registerEmailEditText)
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText)
        registerButton = findViewById(R.id.salvarButton)
        sairButton = findViewById(R.id.sairButton)

        registerButton.setOnClickListener { createAccount() }
        sairButton.setOnClickListener { finish() }
    }

    private fun createAccount() {
        val name = registerNameEditText.text.toString().trim()
        val email = registerEmailEditText.text.toString().trim()
        val password = registerPasswordEditText.text.toString().trim()
        val confirmPassword = registerConfirmPasswordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        salvarUsuarioNoDatabase(user, name, email)
                        updateProfile(user, name)
                        sendEmailVerification(user)
                    }
                    Toast.makeText(
                        this,
                        "Novo usuário cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido"
                    Log.e("FirebaseAuth", "Erro ao cadastrar usuário: $errorMessage")
                    Toast.makeText(
                        this,
                        "Falha ao cadastrar novo usuário: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { ex ->
                Log.e("FirebaseAuth", "Erro ao conectar com o Firebase", ex)
                Toast.makeText(
                    this,
                    "Falha ao conectar com o Firebase: ${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendEmailVerification(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Email de verificação enviado para ${user.email}.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Falha ao enviar o email de verificação.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateProfile(user: FirebaseUser?, displayName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Nome do usuário alterado com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Não foi possível alterar o nome do usuário.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun salvarUsuarioNoDatabase(user: FirebaseUser, nome: String, email: String) {
        val usuario = Usuario(
            key = user.uid,
            nome = nome,
            email = email
        )

        database.child(user.uid).setValue(usuario)
            .addOnFailureListener { error ->
                Log.e("CadastroUsuario", "Erro ao salvar usuário no database", error)
                Toast.makeText(
                    baseContext,
                    "Não foi possível registrar os dados no banco.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
