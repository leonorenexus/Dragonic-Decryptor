package com.dragonic.decryptor.ui.analyzer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.FragmentAnalyzerBinding
import com.dragonic.decryptor.domain.model.AnalysisResult
import com.dragonic.decryptor.util.FileAnalyzer
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyzerFragment : Fragment() {

    private var _binding: FragmentAnalyzerBinding? = null
    private val binding get() = _binding!!
    private var inputData: ByteArray? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { loadFile(it) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyzerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        startRadarAnimation()
    }

    private fun startRadarAnimation() {
        val rotate = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_radar)
        binding.radarSweep.startAnimation(rotate)
    }

    private fun setupClickListeners() {
        binding.btnAnalyze.setOnClickListener {
            if (inputData == null) {
                filePickerLauncher.launch("*/*")
            } else {
                startAnalysis()
            }
        }
        binding.btnCancelAnalysis.setOnClickListener {
            resetUI()
        }
    }

    private fun loadFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.readBytes()
                }
                inputData = bytes
                val fileName = uri.lastPathSegment ?: "unknown_file"
                Toast.makeText(context, "File loaded: $fileName", Toast.LENGTH_SHORT).show()
                startAnalysis(fileName)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startAnalysis(fileName: String = "input_data") {
        val data = inputData ?: "Sample encrypted data for analysis".toByteArray()
        binding.btnAnalyze.visibility = View.GONE
        binding.btnCancelAnalysis.visibility = View.VISIBLE
        binding.tvAnalyzingStatus.text = "ANALYZING ENCRYPTED DATA…"

        // Reset steps
        resetSteps()
        lifecycleScope.launch {
            runAnalysisSteps(data, fileName)
        }
    }

    private fun resetSteps() {
        listOf(
            binding.stepFileFormat, binding.stepEncryption,
            binding.stepEntropy, binding.stepBruteForce, binding.stepResults
        ).forEach { step ->
            step.findViewById<View>(R.id.stepStatus).visibility = View.INVISIBLE
            step.findViewById<View>(R.id.stepProgress).visibility = View.GONE
        }
    }

    private suspend fun runAnalysisSteps(data: ByteArray, fileName: String) {
        val steps = listOf(
            binding.stepFileFormat to "Checking file format",
            binding.stepEncryption to "Detecting encryption type",
            binding.stepEntropy to "Analyzing entropy",
            binding.stepBruteForce to "Brute force estimation",
            binding.stepResults to "Generating results"
        )

        steps.forEachIndexed { index, (stepView, label) ->
            val labelView = stepView.findViewById<android.widget.TextView>(R.id.stepLabel)
            val statusIcon = stepView.findViewById<android.widget.ImageView>(R.id.stepStatus)
            val progressCircle = stepView.findViewById<com.google.android.material.progressindicator.CircularProgressIndicator>(R.id.stepProgress)

            labelView.text = label
            progressCircle.visibility = View.VISIBLE
            statusIcon.visibility = View.INVISIBLE
            delay(600L + index * 200L)
            progressCircle.visibility = View.GONE
            statusIcon.visibility = View.VISIBLE
        }

        val result = withContext(Dispatchers.Default) {
            FileAnalyzer.analyze(data, fileName)
        }
        showResults(result)
    }

    private fun showResults(result: AnalysisResult) {
        binding.tvAnalyzingStatus.text = "ANALYSIS COMPLETE"
        binding.btnAnalyze.visibility = View.VISIBLE
        binding.btnAnalyze.text = "ANALYZE ANOTHER FILE"
        binding.btnCancelAnalysis.visibility = View.GONE

        // Populate File Analysis card
        binding.cardAnalysisResult.visibility = View.VISIBLE
        setInfoRow(binding.rowFile, "File", result.fileName.ifBlank { "text_input.txt" })
        setInfoRow(binding.rowSize, "Size", FileAnalyzer.formatFileSize(result.fileSizeBytes))
        setInfoRow(binding.rowType, "Type", result.fileType)
        setInfoRow(binding.rowEntropy, "Entropy", String.format("%.2f (High)", result.entropy))
        setInfoRow(binding.rowEncryption, "Possible Encryption", result.possibleAlgorithms.take(2).joinToString(" / "))
        setInfoRow(binding.rowConfidence, "Confidence", "${result.confidence}%")

        // Show entropy chart
        binding.cardEntropyChart.visibility = View.VISIBLE
        setupEntropyChart(result.entropyPoints)
    }

    private fun setInfoRow(rowView: View, key: String, value: String) {
        rowView.findViewById<android.widget.TextView>(R.id.rowKey).text = key
        rowView.findViewById<android.widget.TextView>(R.id.rowValue).text = value
    }

    private fun setupEntropyChart(points: List<Float>) {
        val entries = points.mapIndexed { i, v -> Entry(i.toFloat() * 5, v) }
        val dataSet = LineDataSet(entries, "Entropy").apply {
            color = requireContext().getColor(R.color.neon_primary)
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            fillColor = requireContext().getColor(R.color.neon_dim)
            setDrawFilled(true)
            fillAlpha = 80
        }
        binding.entropyChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setBackgroundColor(requireContext().getColor(R.color.bg_card))
            xAxis.textColor = requireContext().getColor(R.color.text_secondary)
            axisLeft.textColor = requireContext().getColor(R.color.text_secondary)
            axisRight.isEnabled = false
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 8f
            gridBackgroundColor = requireContext().getColor(R.color.transparent)
            invalidate()
            animateX(800)
        }
    }

    private fun resetUI() {
        inputData = null
        binding.btnAnalyze.text = "START ANALYSIS"
        binding.btnAnalyze.visibility = View.VISIBLE
        binding.btnCancelAnalysis.visibility = View.GONE
        binding.tvAnalyzingStatus.text = "ANALYZING ENCRYPTED DATA…"
        binding.cardAnalysisResult.visibility = View.GONE
        binding.cardEntropyChart.visibility = View.GONE
        resetSteps()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
