package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
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
import com.ifpr.androidapptemplate.theme.AppThemeOption
import com.ifpr.androidapptemplate.theme.ThemeManager

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private var usersReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth
    private var userListener: ValueEventListener? = null
    private var selectedTheme: AppThemeOption = AppThemeOption.CLASSIC

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        usersReference = runCatching {
            FirebaseDatabase.getInstance().getReference("users")
        }.onFailure { error ->
            Log.e("DatabaseReference", "Erro ao obter referencia para o Firebase Database", error)
            Toast.makeText(context, "Erro ao acessar o Firebase Database", Toast.LENGTH_SHORT)
                .show()
        }.getOrNull()

        val user = auth.currentUser

        binding.registerEmailEditText.isEnabled = false
        binding.logoutButton.visibility = if (user != null) View.VISIBLE else View.GONE

        user?.let {
            Glide.with(this)
                .load(it.photoUrl)
                .placeholder(R.drawable.ic_profile_avatar)
                .into(binding.userProfileImageView)

            binding.registerNameEditText.setText(it.displayName)
            binding.registerEmailEditText.setText(it.email)

            recuperarDadosUsuario(it.uid)
            checarPermissaoAdmin(it)
        }

        binding.updateProfileButton.setOnClickListener { updateUser() }
        binding.logoutButton.setOnClickListener { signOut() }

        return binding.root
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
        // Nenhuma acao adicional necessaria aqui no momento
    }

    override fun onDestroyView() {
        userListener?.let { listener ->
            auth.currentUser?.uid?.let { uid -> usersReference?.child(uid)?.removeEventListener(listener) }
        }
        userListener = null
        super.onDestroyView()
        _binding = null
    }

    private fun recuperarDadosUsuario(usuarioKey: String) {
        val reference = usersReference ?: return
        userListener?.let { reference.child(usuarioKey).removeEventListener(it) }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        if (!it.nome.isNullOrBlank()) binding.registerNameEditText.setText(it.nome)
                        if (!it.email.isNullOrBlank()) binding.registerEmailEditText.setText(it.email)
                        binding.registerEnderecoEditText.setText(it.endereco ?: "")
                        binding.registerTelefoneEditText.setText(it.telefone ?: "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
            }
        }
        userListener = listener
        reference.child(usuarioKey).addValueEventListener(listener)
    }

    private fun updateUser() {
        val name = binding.registerNameEditText.text.toString().trim()
        val endereco = binding.registerEnderecoEditText.text.toString().trim()
        val telefone = binding.registerTelefoneEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Informe o nome do usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser

        if (user != null) {
            updateProfile(user, name, endereco, telefone)
        } else {
            Toast.makeText(context, "Nao foi possivel encontrar o usuario logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser, displayName: String, endereco: String, telefone: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(user, displayName, endereco, telefone)
                } else {
                    Toast.makeText(
                        context,
                        "Nao foi possivel atualizar os dados do usuario.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(user: FirebaseUser, displayName: String, endereco: String, telefone: String) {
        val reference = usersReference ?: run {
            Toast.makeText(
                context,
                "Referencia ao Firebase Database indisponivel",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val usuario = Usuario(
            key = user.uid,
            nome = displayName,
            email = user.email,
            endereco = endereco,
            telefone = telefone
        )

        reference.child(user.uid).setValue(usuario)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Usuario atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseError", "Falha ao atualizar usuario", error)
                Toast.makeText(
                    context,
                    "Falha ao atualizar o usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checarPermissaoAdmin(user: FirebaseUser) {
        user.getIdToken(true)
            .addOnSuccessListener { result ->
                val isAdmin = result.claims["admin"] == true
                binding.adminThemeCard.isVisible = isAdmin
                if (isAdmin) {
                    prepararControlesDeTema()
                }
            }
            .addOnFailureListener { error ->
                Log.e("PerfilUsuario", "Falha ao verificar privilegios de admin", error)
                binding.adminThemeCard.isVisible = false
            }
    }

    private fun prepararControlesDeTema() {
        selectedTheme = ThemeManager.getSavedTheme(requireContext())
        atualizarSelecaoDeTema(selectedTheme)
        atualizarStatusDeTema(selectedTheme)

        ThemeManager.fetchRemoteThemeOnce(requireContext()) { remoteTheme ->
            selectedTheme = remoteTheme
            atualizarSelecaoDeTema(remoteTheme)
            atualizarStatusDeTema(remoteTheme)
        }

        binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedTheme = when (checkedId) {
                R.id.neonThemeButton -> AppThemeOption.NEON
                else -> AppThemeOption.CLASSIC
            }
            atualizarStatusDeTema(selectedTheme)
        }

        binding.applyThemeButton.setOnClickListener {
            ThemeManager.updateRemoteTheme(requireContext(), selectedTheme) { success ->
                if (success) {
                    Toast.makeText(
                        context,
                        getString(R.string.theme_update_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().recreate()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.theme_update_failure),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun atualizarSelecaoDeTema(themeOption: AppThemeOption) {
        when (themeOption) {
            AppThemeOption.CLASSIC -> binding.themeToggleGroup.check(R.id.classicThemeButton)
            AppThemeOption.NEON -> binding.themeToggleGroup.check(R.id.neonThemeButton)
        }
    }

    private fun atualizarStatusDeTema(themeOption: AppThemeOption) {
        val themeLabel = when (themeOption) {
            AppThemeOption.CLASSIC -> getString(R.string.theme_option_classic)
            AppThemeOption.NEON -> getString(R.string.theme_option_neon)
        }
        binding.themeStatusTextView.text = getString(R.string.theme_current_label, themeLabel)
    }
}
