package com.dragonic.decryptor.ui.home

import android.content.ClipboardManager
import android.content.Context
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
import com.dragonic.decryptor.databinding.FragmentHomeBinding
import com.dragonic.decryptor.domain.model.DecryptAlgorithm
import com.dragonic.decryptor.domain.model.DecryptionRecord
import com.dragonic.decryptor.util.DecryptionEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedAlgorithm = DecryptAlgorithm.AUTO
    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadFileContent(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlgorithmDropdown()
        setupClickListeners()
    }

    private fun setupAlgorithmDropdown() {
        val algorithms = DecryptAlgorithm.values().map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, algorithms)
        binding.algorithmDropdown.setAdapter(adapter)
        binding.algorithmDropdown.setText(DecryptAlgorithm.AUTO.label, false)
        binding.algorithmDropdown.setOnItemClickListener { _, _, pos, _ ->
            selectedAlgorithm = DecryptAlgorithm.values()[pos]
        }
    }

    private fun setupClickListeners() {
        binding.btnDecryptNow.setOnClickListener { performDecryption() }

        binding.btnPaste.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!text.isNullOrBlank()) {
                binding.inputText.setText(text)
                Toast.makeText(context, "Text pasted from clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.toolsFragment)
        }

        binding.quickAutoDetect.setOnClickListener {
            selectedAlgorithm = DecryptAlgorithm.AUTO
            binding.algorithmDropdown.setText(DecryptAlgorithm.AUTO.label, false)
            performDecryption()
        }

        binding.quickFileDecrypt.setOnClickListener {
            findNavController().navigate(R.id.fileDecryptorFragment)
        }

        binding.quickHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        binding.quickAnalyzer.setOnClickListener {
            findNavController().navigate(R.id.analyzerFragment)
        }
    }

    private fun performDecryption() {
        val input = binding.inputText.text?.toString()?.trim() ?: ""
        if (input.isEmpty()) {
            Toast.makeText(context, "Please enter or paste encrypted data", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnDecryptNow.isEnabled = false
        binding.btnDecryptNow.text = "DECRYPTING…"

        lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                DecryptionEngine.decrypt(selectedAlgorithm, input)
            }

            // Save to history
            repo.insertHistory(
                DecryptionRecord(
                    algorithm = selectedAlgorithm.label,
                    inputData = input,
                    outputData = result.output,
                    timeTakenMs = result.timeTakenMs,
                    isSuccess = result.success
                )
            )

            binding.btnDecryptNow.isEnabled = true
            binding.btnDecryptNow.text = "🔓  DECRYPT NOW"

            if (result.success) {
                val bundle = Bundle().apply {
                    putString("originalData", input)
                    putString("decryptedData", result.output)
                    putString("algorithm", selectedAlgorithm.label)
                }
                findNavController().navigate(R.id.resultFragment, bundle)
            } else {
                Toast.makeText(context, "Decryption failed: ${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFileContent(uri: Uri) {
        lifecycleScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                }
                binding.inputText.setText(content ?: "")
                Toast.makeText(context, "File loaded", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
