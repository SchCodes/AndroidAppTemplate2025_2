package com.ifpr.androidapptemplate.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.databinding.FragmentNotificationsBinding
import com.ifpr.androidapptemplate.notification.NotificationEntry
import com.ifpr.androidapptemplate.notification.NotificationRepository

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val adapter = NotificationsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecycler.adapter = adapter

        binding.clearNotificationsButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                renderNotifications(emptyList())
                return@setOnClickListener
            }
            NotificationRepository.clearNotifications(requireContext(), uid)
            renderNotifications(emptyList())
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val items = NotificationRepository.getNotifications(requireContext(), uid)
        renderNotifications(items)
    }

    private fun renderNotifications(items: List<NotificationEntry>) {
        adapter.submitList(items)
        val empty = items.isEmpty()
        binding.notificationsEmptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.notificationsRecycler.visibility = if (empty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
