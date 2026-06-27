package com.dragonic.decryptor.ui.tools

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dragonic.decryptor.DragonicApp
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.FragmentFileDecryptorBinding
import com.dragonic.decryptor.domain.model.DecryptAlgorithm
import com.dragonic.decryptor.domain.model.DecryptionRecord
import com.dragonic.decryptor.domain.model.SavedFile
import com.dragonic.decryptor.util.DecryptionEngine
import com.dragonic.decryptor.util.FileAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileDecryptorFragment : Fragment() {

    private var _binding: FragmentFileDecryptorBinding? = null
    private val binding get() = _binding!!
    private var selectedFileBytes: ByteArray? = null
    private var selectedFileName = ""
    private var selectedAlgorithm = DecryptAlgorithm.AES_256
    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { loadFile(it) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFileDecryptorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlgorithmDropdown()
        setupClickListeners()

        // Pre-select algorithm from tools nav
        arguments?.getString("toolId")?.let { toolId ->
            val algo = mapToolIdToAlgorithm(toolId)
            selectedAlgorithm = algo
            binding.methodDropdown.setText(algo.label, false)
        }
    }

    private fun setupAlgorithmDropdown() {
        val algorithms = DecryptAlgorithm.values().map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, algorithms)
        binding.methodDropdown.setAdapter(adapter)
        binding.methodDropdown.setText(DecryptAlgorithm.AES_256.label, false)
        binding.methodDropdown.setOnItemClickListener { _, _, pos, _ ->
            selectedAlgorithm = DecryptAlgorithm.values()[pos]
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnChange.setOnClickListener { filePickerLauncher.launch("*/*") }
        binding.btnDecryptFile.setOnClickListener { performDecryption() }
    }

    private fun loadFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.readBytes()
                }
                selectedFileBytes = bytes
                selectedFileName = uri.lastPathSegment ?: "selected_file"
                binding.tvSelectedFileName.text = selectedFileName
                binding.tvSelectedFileSize.text = FileAnalyzer.formatFileSize(bytes?.size?.toLong() ?: 0L)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performDecryption() {
        val fileBytes = selectedFileBytes
        val key = binding.etKey.text?.toString() ?: ""
        val iv = binding.etIv.text?.toString() ?: ""

        if (fileBytes == null) {
            Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnDecryptFile.isEnabled = false
        binding.btnDecryptFile.text = "DECRYPTING…"

        lifecycleScope.launch {
            val inputStr = String(fileBytes, Charsets.UTF_8).let {
                if (it.isBlank()) android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT) else it
            }

            val result = withContext(Dispatchers.Default) {
                DecryptionEngine.decrypt(selectedAlgorithm, inputStr, key, iv)
            }

            binding.btnDecryptFile.isEnabled = true
            binding.btnDecryptFile.text = "DECRYPT FILE"

            // Save history
            repo.insertHistory(DecryptionRecord(
                algorithm = selectedAlgorithm.label,
                inputData = inputStr.take(200),
                outputData = if (result.success) result.output.take(200) else result.error,
                timeTakenMs = result.timeTakenMs,
                isSuccess = result.success,
                isFile = true,
                fileName = selectedFileName
            ))

            if (result.success) {
                // Save file
                val outName = "decrypted_${selectedFileName}"
                val outFile = withContext(Dispatchers.IO) {
                    val dir = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
                    val f = File(dir, outName)
                    f.writeText(result.output)
                    f
                }
                repo.insertFile(SavedFile(name = outName, path = outFile.absolutePath, size = outFile.length(), mimeType = "text/plain"))

                // Show result card
                binding.cardResult.visibility = View.VISIBLE
                setRow(binding.rowStatus, "Status", "Success")
                setRow(binding.rowSize, "Size", "${FileAnalyzer.formatFileSize(fileBytes.size.toLong())} → ${FileAnalyzer.formatFileSize(result.output.length.toLong())}")
                setRow(binding.rowTime, "Time", "${result.timeTakenMs}ms")
                setRow(binding.rowAlgorithm, "Algorithm", selectedAlgorithm.label)

                val bundle = Bundle().apply {
                    putString("originalData", inputStr.take(300))
                    putString("decryptedData", result.output)
                    putString("algorithm", selectedAlgorithm.label)
                }
                findNavController().navigate(R.id.resultFragment, bundle)
            } else {
                Toast.makeText(context, "Decryption failed: ${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setRow(rowView: View, key: String, value: String) {
        rowView.findViewById<android.widget.TextView>(R.id.rowKey).text = key
        rowView.findViewById<android.widget.TextView>(R.id.rowValue).text = value
    }

    private fun mapToolIdToAlgorithm(toolId: String): DecryptAlgorithm = when (toolId) {
        "aes" -> DecryptAlgorithm.AES_256
        "des" -> DecryptAlgorithm.DES
        "3des" -> DecryptAlgorithm.TRIPLE_DES
        "blowfish" -> DecryptAlgorithm.BLOWFISH
        "rc4" -> DecryptAlgorithm.RC4
        "chacha20" -> DecryptAlgorithm.CHACHA20
        "xor" -> DecryptAlgorithm.XOR
        "base64" -> DecryptAlgorithm.BASE64
        "base32" -> DecryptAlgorithm.BASE32
        "hex" -> DecryptAlgorithm.HEX
        "binary" -> DecryptAlgorithm.BINARY
        "octal" -> DecryptAlgorithm.OCTAL
        "rot13" -> DecryptAlgorithm.ROT13
        "url" -> DecryptAlgorithm.URL
        "html" -> DecryptAlgorithm.HTML
        "unicode" -> DecryptAlgorithm.UNICODE
        "jwt" -> DecryptAlgorithm.JWT
        "gzip" -> DecryptAlgorithm.GZIP
        "zlib" -> DecryptAlgorithm.ZLIB
        else -> DecryptAlgorithm.AUTO
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
