package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null

    private lateinit var userProfileImageView: ImageView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        userProfileImageView = view.findViewById(R.id.userProfileImageView)
        registerNameEditText = view.findViewById(R.id.registerNameEditText)
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText)
        registerEnderecoEditText = view.findViewById(R.id.registerEnderecoEditText)
        registerPasswordEditText = view.findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = view.findViewById(R.id.registerConfirmPasswordEditText)
        registerButton = view.findViewById(R.id.salvarButton)
        sairButton = view.findViewById(R.id.sairButton)

        try {
            usersReference = FirebaseDatabase.getInstance().getReference("users")
        } catch (e: Exception) {
            Log.e("DatabaseReference", "Erro ao obter referência para o Firebase DatabaseReference", e)
            // Trate o erro conforme necessário, por exemplo:
            Toast.makeText(context, "Erro ao acessar o Firebase DatabaseReference", Toast.LENGTH_SHORT).show()
        }

        // Acessar currentUser
        val user = auth.currentUser

        if (user != null) {
            sairButton.visibility = View.VISIBLE
            registerPasswordEditText.visibility = View.GONE
            registerConfirmPasswordEditText.visibility = View.GONE
            registerEmailEditText.isEnabled = false
        }

        user?.let {
            // Exibe a foto do perfil usando a biblioteca Glide
            Glide.with(this).load(it.photoUrl).into(userProfileImageView)
        }

        registerButton.setOnClickListener {
            updateUser()
        }

        sairButton.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(
            context,
            "Logout realizado com sucesso!",
            Toast.LENGTH_SHORT
        ).show()

        requireActivity().finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exibe os dados do usuario logado, se disponivel

        // Acessar currentUser
        var userFirebase = auth.currentUser
        if(userFirebase != null){
            registerNameEditText.setText(userFirebase.displayName)
            registerEmailEditText.setText(userFirebase.email)

            recuperarDadosUsuario(userFirebase.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun recuperarDadosUsuario(usuarioKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.child(usuarioKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        registerEnderecoEditText.setText(it.endereco ?: "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
            }
        })
    }

    private fun updateUser() {
        val name = registerNameEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()

        // Acessar currentUser
        val user = auth.currentUser

        // Verifica se o usuário atual já está definido
        if (user != null) {
            // Se o usuário já existe, atualiza os dados
            updateProfile(user, name, endereco)
        } else {
            Toast.makeText(context, "Não foi possível encontrar o usuário logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser?, displayName: String, endereco: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        val usuario = Usuario(user?.uid.toString() , displayName, user?.email, endereco, )

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(usuario)
                    Toast.makeText(context, "Nome do usuario alterado com sucesso.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não foi possivel alterar o nome do usuario.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        if (usuario.key != null) {
            usersReference.child(usuario.key.toString()).setValue(usuario)
                .addOnSuccessListener {
                    Toast.makeText(context, "Usuario atualizado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao atualizar o usuario", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "ID invalido", Toast.LENGTH_SHORT).show()
        }
    }
}
