package com.dragonic.decryptor.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.ItemHistoryBinding
import com.dragonic.decryptor.domain.model.DecryptionRecord
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onFavoriteClick: (DecryptionRecord) -> Unit,
    private val onDeleteClick: (DecryptionRecord) -> Unit
) : ListAdapter<DecryptionRecord, HistoryAdapter.ViewHolder>(HistoryDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: DecryptionRecord) {
            binding.tvAlgorithm.text = record.algorithm
            binding.tvFileName.text = if (record.isFile) record.fileName else record.inputData.take(30) + if (record.inputData.length > 30) "…" else ""
            binding.tvTime.text = timeFormat.format(Date(record.timestamp))
            binding.tvStatus.text = if (record.isSuccess) "Success" else "Failed"
            binding.tvStatus.setTextColor(
                binding.root.context.getColor(
                    if (record.isSuccess) R.color.status_success else R.color.status_error
                )
            )
            binding.btnStar.setImageResource(
                if (record.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star
            )
            binding.btnStar.setOnClickListener { onFavoriteClick(record) }
            binding.root.setOnLongClickListener {
                onDeleteClick(record)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class HistoryDiffCallback : DiffUtil.ItemCallback<DecryptionRecord>() {
        override fun areItemsTheSame(old: DecryptionRecord, new: DecryptionRecord) = old.id == new.id
        override fun areContentsTheSame(old: DecryptionRecord, new: DecryptionRecord) = old == new
    }
}
