package com.dragonic.decryptor.ui.result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dragonic.decryptor.DragonicApp
import com.dragonic.decryptor.databinding.FragmentResultBinding
import com.dragonic.decryptor.domain.model.SavedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private var originalData = ""
    private var decryptedData = ""
    private var algorithm = ""

    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            originalData = it.getString("originalData", "")
            decryptedData = it.getString("decryptedData", "")
            algorithm = it.getString("algorithm", "Unknown")
        }
        populateUI()
        setupClickListeners()
    }

    private fun populateUI() {
        binding.tvOriginalData.text = if (originalData.length > 200) originalData.take(200) + "…" else originalData
        binding.tvDecryptedData.text = decryptedData

        setInfoRow(binding.rowAlgorithm, "Algorithm", algorithm)
        setInfoRow(binding.rowKey, "Key", "••••••••••••••••")
        setInfoRow(binding.rowIv, "IV", "Auto Detected")
        setInfoRow(binding.rowTime, "Time", "< 1s")
        setInfoRow(binding.rowStatus, "Status", "✓ Success")
        binding.rowStatus.findViewById<android.widget.TextView>(com.dragonic.decryptor.R.id.rowValue)
            .setTextColor(requireContext().getColor(com.dragonic.decryptor.R.color.status_success))
    }

    private fun setInfoRow(rowView: View, key: String, value: String) {
        rowView.findViewById<android.widget.TextView>(com.dragonic.decryptor.R.id.rowKey).text = key
        rowView.findViewById<android.widget.TextView>(com.dragonic.decryptor.R.id.rowValue).text = value
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnCopy.setOnClickListener { copyToClipboard(decryptedData) }
        binding.btnCopyOriginal.setOnClickListener { copyToClipboard(originalData) }
        binding.btnViewResult.setOnClickListener { copyToClipboard(decryptedData) }

        binding.btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, decryptedData)
                putExtra(Intent.EXTRA_SUBJECT, "Decrypted data - Dragonic Decryptor")
            }
            startActivity(Intent.createChooser(intent, "Share decrypted data"))
        }

        binding.btnSave.setOnClickListener { saveToFile() }
        binding.btnSaveFile.setOnClickListener { saveToFile() }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Dragonic Decrypted", text))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun saveToFile() {
        lifecycleScope.launch {
            try {
                val fileName = "decrypted_${System.currentTimeMillis()}.txt"
                val file = withContext(Dispatchers.IO) {
                    val dir = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
                    val f = File(dir, fileName)
                    f.writeText(decryptedData)
                    f
                }
                repo.insertFile(
                    SavedFile(
                        name = fileName,
                        path = file.absolutePath,
                        size = file.length(),
                        mimeType = "text/plain"
                    )
                )
                Toast.makeText(context, "Saved as $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
