package com.gamehouse.doctor

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Menampilkan laporan panjang yang bisa digulir, disalin, atau dibagikan. */
class ReportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Laporan"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        setTitle(title)

        val text = TextView(this).apply {
            this.text = body
            textSize = 13f
            setTextIsSelectable(true)
            setPadding(0, 16, 0, 16)
        }
        val copy = Button(this).apply {
            this.text = "Salin"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(title, body))
                Toast.makeText(this@ReportActivity, "Laporan disalin.", Toast.LENGTH_SHORT).show()
            }
        }
        val share = Button(this).apply {
            this.text = "Bagikan"
            setOnClickListener {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                try {
                    startActivity(Intent.createChooser(send, "Bagikan laporan"))
                } catch (e: Exception) {
                    Toast.makeText(this@ReportActivity, "Tidak bisa membagikan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        val close = Button(this).apply {
            this.text = "Tutup"
            setOnClickListener { finish() }
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(share, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(close, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        val scroll = ScrollView(this).apply { addView(text) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            addView(row)
            addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(root)
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"

        fun open(ctx: Context, title: String, body: String) {
            ctx.startActivity(
                Intent(ctx, ReportActivity::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_BODY, body)
            )
        }
    }
}
