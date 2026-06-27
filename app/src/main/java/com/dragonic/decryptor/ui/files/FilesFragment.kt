package com.dragonic.decryptor.ui.files

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragonic.decryptor.DragonicApp
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.FragmentFilesBinding
import com.dragonic.decryptor.domain.model.SavedFile
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FilesFragment : Fragment() {

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FilesAdapter
    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeFiles()

        binding.fabAdd.setOnClickListener {
            Toast.makeText(context, "Decrypt a file first to save it here", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = FilesAdapter(
            onMoreClick = { file, anchorView -> showFileMenu(file, anchorView) }
        )
        binding.filesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@FilesFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.searchFiles.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                lifecycleScope.launch {
                    if (query.isBlank()) repo.getAllFiles().collectLatest { adapter.submitList(it) }
                    else repo.searchFiles(query).collectLatest { adapter.submitList(it) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeFiles() {
        lifecycleScope.launch {
            repo.getAllFiles().collectLatest { adapter.submitList(it) }
        }
    }

    private fun showFileMenu(file: SavedFile, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Open")
        popup.menu.add("Share")
        popup.menu.add(if (file.isFavorite) "Unfavorite" else "Favorite")
        popup.menu.add("Delete")
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Open" -> openFile(file)
                "Share" -> shareFile(file)
                "Favorite", "Unfavorite" -> {
                    lifecycleScope.launch { repo.toggleFavoriteFile(file.id, !file.isFavorite) }
                }
                "Delete" -> {
                    lifecycleScope.launch {
                        repo.deleteFile(file.id)
                        File(file.path).delete()
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun openFile(file: SavedFile) {
        try {
            val f = File(file.path)
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", f)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, file.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: SavedFile) {
        try {
            val f = File(file.path)
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", f)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = file.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share file"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
