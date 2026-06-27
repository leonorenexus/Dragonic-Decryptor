package com.dragonic.decryptor.ui.tools

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.FragmentToolsBinding
import com.dragonic.decryptor.domain.model.ToolCategory
import com.dragonic.decryptor.domain.model.ToolItem
import com.dragonic.decryptor.util.ToolsAdapter
import com.dragonic.decryptor.util.ToolsAdapter.ToolListItem

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ToolsAdapter
    private var currentCategory: ToolCategory? = null
    private var allItems: List<ToolListItem> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupSearch()
        loadTools()
    }

    private fun setupRecyclerView() {
        adapter = ToolsAdapter { tool ->
            // Navigate to file decryptor with pre-selected algorithm
            val bundle = Bundle().apply { putString("toolId", tool.id) }
            findNavController().navigate(R.id.fileDecryptorFragment, bundle)
        }
        binding.toolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ToolsFragment.adapter
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { filterByCategory(null) }
        binding.chipSymmetric.setOnClickListener { filterByCategory(ToolCategory.SYMMETRIC) }
        binding.chipAsymmetric.setOnClickListener { filterByCategory(ToolCategory.ASYMMETRIC) }
        binding.chipHash.setOnClickListener { filterByCategory(ToolCategory.HASH) }
        binding.chipOther.setOnClickListener { filterByCategory(ToolCategory.OTHER) }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) allItems
                else allItems.filter {
                    it is ToolListItem.Item && (it.tool.name.lowercase().contains(query) || it.tool.description.lowercase().contains(query))
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterByCategory(category: ToolCategory?) {
        currentCategory = category
        updateChipState(category)
        val filtered = if (category == null) allItems
        else allItems.filter { item ->
            when (item) {
                is ToolListItem.Header -> {
                    val nextItems = allItems.dropWhile { it != item }.drop(1)
                    nextItems.filterIsInstance<ToolListItem.Item>().any { it.tool.category == category }.also { }
                }
                is ToolListItem.Item -> item.tool.category == category
            }
        }
        adapter.submitList(filtered)
    }

    private fun updateChipState(selected: ToolCategory?) {
        val neonColor = requireContext().getColor(R.color.neon_primary)
        val bgColor = requireContext().getColor(R.color.bg_card)
        val neonTextColor = requireContext().getColor(R.color.bg_primary)
        val grayTextColor = requireContext().getColor(R.color.text_secondary)
        listOf(
            binding.chipAll to null,
            binding.chipSymmetric to ToolCategory.SYMMETRIC,
            binding.chipAsymmetric to ToolCategory.ASYMMETRIC,
            binding.chipHash to ToolCategory.HASH,
            binding.chipOther to ToolCategory.OTHER
        ).forEach { (chip, cat) ->
            val isSelected = cat == selected
            chip.setChipBackgroundColorResource(if (isSelected) R.color.neon_primary else R.color.bg_card)
            chip.setTextColor(if (isSelected) neonTextColor else grayTextColor)
        }
    }

    private fun loadTools() {
        allItems = buildToolList()
        adapter.submitList(allItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
