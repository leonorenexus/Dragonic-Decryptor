package com.dragonic.decryptor.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.ItemToolBinding
import com.dragonic.decryptor.databinding.ItemToolHeaderBinding
import com.dragonic.decryptor.domain.model.ToolCategory
import com.dragonic.decryptor.domain.model.ToolItem

class ToolsAdapter(
    private val onToolClick: (ToolItem) -> Unit
) : ListAdapter<ToolsAdapter.ToolListItem, RecyclerView.ViewHolder>(ToolDiffCallback()) {

    sealed class ToolListItem {
        data class Header(val title: String) : ToolListItem()
        data class Item(val tool: ToolItem) : ToolListItem()
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ToolListItem.Header -> VIEW_TYPE_HEADER
        is ToolListItem.Item -> VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderVH(ItemToolHeaderBinding.inflate(inflater, parent, false))
            else -> ItemVH(ItemToolBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ToolListItem.Header -> (holder as HeaderVH).bind(item.title)
            is ToolListItem.Item  -> (holder as ItemVH).bind(item.tool)
        }
    }

    inner class HeaderVH(private val b: ItemToolHeaderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(title: String) { b.headerTitle.text = title }
    }

    inner class ItemVH(private val b: ItemToolBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(tool: ToolItem) {
            b.toolName.text = tool.name
            b.toolDescription.text = tool.description
            b.toolIcon.setImageResource(tool.iconRes)
            b.root.setOnClickListener { onToolClick(tool) }
        }
    }

    class ToolDiffCallback : DiffUtil.ItemCallback<ToolListItem>() {
        override fun areItemsTheSame(old: ToolListItem, new: ToolListItem): Boolean = when {
            old is ToolListItem.Header && new is ToolListItem.Header -> old.title == new.title
            old is ToolListItem.Item   && new is ToolListItem.Item   -> old.tool.id == new.tool.id
            else -> false
        }
        override fun areContentsTheSame(old: ToolListItem, new: ToolListItem): Boolean = old == new
    }
}

fun buildToolList(): List<ToolsAdapter.ToolListItem> = listOf(
    ToolsAdapter.ToolListItem.Header("SYMMETRIC DECRYPTION"),
    ToolsAdapter.ToolListItem.Item(ToolItem("aes",      "AES Decrypt",        "Advanced Encryption Standard",  ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("des",      "DES Decrypt",        "Data Encryption Standard",      ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("3des",     "3DES Decrypt",       "Triple DES Decryption",         ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("blowfish", "Blowfish Decrypt",   "Blowfish Algorithm",            ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("rc4",      "RC4 Decrypt",        "Rivest Cipher 4",               ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("chacha20", "ChaCha20 Decrypt",   "ChaCha20 Algorithm",            ToolCategory.SYMMETRIC, R.drawable.ic_lock)),
    ToolsAdapter.ToolListItem.Item(ToolItem("xor",      "XOR Decrypt",        "XOR Cipher",                    ToolCategory.SYMMETRIC, R.drawable.ic_lock)),

    ToolsAdapter.ToolListItem.Header("ASYMMETRIC DECRYPTION"),
    ToolsAdapter.ToolListItem.Item(ToolItem("rsa",      "RSA Decrypt",        "RSA Algorithm (key required)",  ToolCategory.ASYMMETRIC, R.drawable.ic_lock)),

    ToolsAdapter.ToolListItem.Header("HASH TOOLS"),
    ToolsAdapter.ToolListItem.Item(ToolItem("md5",      "MD5 Hash",           "Message Digest 5",              ToolCategory.HASH, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("sha1",     "SHA-1 Hash",         "Secure Hash Algorithm 1",       ToolCategory.HASH, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("sha256",   "SHA-256 Hash",       "Secure Hash Algorithm 256",     ToolCategory.HASH, R.drawable.ic_tools)),

    ToolsAdapter.ToolListItem.Header("DECODERS & OTHER"),
    ToolsAdapter.ToolListItem.Item(ToolItem("base64",   "Base64 Decode",      "Decode Base64 encoded data",    ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("base32",   "Base32 Decode",      "Decode Base32 encoded data",    ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("hex",      "Hex Decode",         "Decode hexadecimal data",       ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("binary",   "Binary Decode",      "Decode binary data",            ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("octal",    "Octal Decode",       "Decode octal data",             ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("rot13",    "ROT13",              "ROT13 Cipher",                  ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("url",      "URL Decode",         "URL / Percent Decode",          ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("html",     "HTML Decode",        "HTML Entity Decode",            ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("unicode",  "Unicode Decode",     "Unicode Escape Decode",         ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("jwt",      "JWT Decode",         "JSON Web Token Decode",         ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("gzip",     "Gzip Decompress",    "GZIP Decompression",            ToolCategory.OTHER, R.drawable.ic_tools)),
    ToolsAdapter.ToolListItem.Item(ToolItem("zlib",     "Zlib Decompress",    "Zlib Decompression",            ToolCategory.OTHER, R.drawable.ic_tools)),
)
