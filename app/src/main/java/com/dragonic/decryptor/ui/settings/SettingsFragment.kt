package com.dragonic.decryptor.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dragonic.decryptor.DragonicApp
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val repo by lazy { (requireActivity().application as DragonicApp).repository }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()
    }

    private fun setupSettings() {
        // GENERAL
        setToggleRow(binding.rowDarkTheme, "Dark Theme", true, false) {}
        setToggleRow(binding.rowAutoSave, "Auto Save Result", true, true) {}
        setToggleRow(binding.rowSaveHistory, "Save History", true, true) {}
        setToggleRow(binding.rowAutoDetect, "Auto Detect Algorithm", true, true) {}
        setToggleRow(binding.rowVibrate, "Vibrate", false, false) {}
        setActionRow(binding.rowLanguage, "Language", "System Default") {}

        // SECURITY
        setActionRow(binding.rowClearHistory, "Clear History", "") {
            showConfirmDialog("Clear History", "This will delete all decryption history.") {
                lifecycleScope.launch {
                    repo.clearAllHistory()
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
        setActionRow(binding.rowClearFiles, "Clear Saved Files", "") {
            showConfirmDialog("Clear Saved Files", "This will delete all saved files.") {
                lifecycleScope.launch {
                    repo.clearAllFiles()
                    Toast.makeText(context, "Saved files cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
        setToggleRow(binding.rowLockApp, "Lock App", false, false) {}

        // ABOUT
        setActionRow(binding.rowAppVersion, "App Version", "1.0.0") {}
        setActionRow(binding.rowDeveloper, "Developer", "Dragonic Team") {}
    }

    private fun setToggleRow(rowView: View, label: String, hasToggle: Boolean, checked: Boolean, onChange: (Boolean) -> Unit) {
        rowView.findViewById<android.widget.TextView>(R.id.settingLabel).text = label
        val toggle = rowView.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.settingToggle)
        toggle.isChecked = checked
        toggle.setOnCheckedChangeListener { _, isChecked -> onChange(isChecked) }
    }

    private fun setActionRow(rowView: View, label: String, value: String, onClick: () -> Unit) {
        rowView.findViewById<android.widget.TextView>(R.id.settingLabel).text = label
        rowView.findViewById<android.widget.TextView>(R.id.settingValue).text = value
        rowView.setOnClickListener { onClick() }
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
