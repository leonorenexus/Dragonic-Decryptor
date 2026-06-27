package com.dragonic.decryptor.ui.files

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dragonic.decryptor.databinding.ItemFileBinding
import com.dragonic.decryptor.domain.model.SavedFile
import com.dragonic.decryptor.util.FileAnalyzer
import java.text.SimpleDateFormat
import java.util.*

class FilesAdapter(
    private val onMoreClick: (SavedFile, View) -> Unit
) : ListAdapter<SavedFile, FilesAdapter.ViewHolder>(FileDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: SavedFile) {
            binding.tvFileName.text = file.name
            binding.tvFileSize.text = FileAnalyzer.formatFileSize(file.size)
            binding.tvFileDate.text = dateFormat.format(Date(file.savedAt))
            binding.btnMore.setOnClickListener { onMoreClick(file, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class FileDiffCallback : DiffUtil.ItemCallback<SavedFile>() {
        override fun areItemsTheSame(old: SavedFile, new: SavedFile) = old.id == new.id
        override fun areContentsTheSame(old: SavedFile, new: SavedFile) = old == new
    }
}
