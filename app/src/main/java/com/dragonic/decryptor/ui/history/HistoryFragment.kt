package com.dragonic.decryptor.ui.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragonic.decryptor.DragonicApp
import com.dragonic.decryptor.databinding.FragmentHistoryBinding
import com.dragonic.decryptor.domain.model.DecryptionRecord
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter
    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        setupSearch()
        observeHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onFavoriteClick = { record ->
                lifecycleScope.launch {
                    repo.toggleFavoriteHistory(record.id, !record.isFavorite)
                }
            },
            onDeleteClick = { record ->
                lifecycleScope.launch { repo.deleteHistory(record.id) }
            }
        )
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupChips() {
        binding.chipAll.setOnClickListener { observeHistory() }
        binding.chipSuccess.setOnClickListener {
            lifecycleScope.launch {
                repo.getSuccessHistory().collectLatest { updateList(it) }
            }
        }
        binding.chipFailed.setOnClickListener {
            lifecycleScope.launch {
                repo.getFailedHistory().collectLatest { updateList(it) }
            }
        }
        binding.chipFile.setOnClickListener {
            lifecycleScope.launch {
                repo.getFileHistory().collectLatest { updateList(it) }
            }
        }
        binding.chipFavorite.setOnClickListener {
            lifecycleScope.launch {
                repo.getFavoriteHistory().collectLatest { updateList(it) }
            }
        }
    }

    private fun setupSearch() {
        binding.searchHistory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.isNotBlank()) {
                    lifecycleScope.launch {
                        repo.searchHistory(query).collectLatest { updateList(it) }
                    }
                } else {
                    observeHistory()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            repo.getAllHistory().collectLatest { updateList(it) }
        }
    }

    private fun updateList(list: List<DecryptionRecord>) {
        adapter.submitList(list)
        binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.historyRecyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
