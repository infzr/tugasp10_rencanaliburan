package com.example.rencanaliburan

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etDestinasi: EditText
    private lateinit var etBudget: EditText
    private lateinit var etDurasi: EditText
    private lateinit var etTahun: EditText
    private lateinit var etKataSandi: EditText
    private lateinit var tvHasil: TextView
    private lateinit var btnSimpan: Button

    private var isFormatting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hubungkan view XML
        etDestinasi = findViewById(R.id.etDestinasi)
        etBudget = findViewById(R.id.etBudget)
        etDurasi = findViewById(R.id.etDurasi)
        etTahun = findViewById(R.id.etTahun)
        etKataSandi = findViewById(R.id.etKataSandi)
        tvHasil = findViewById(R.id.tvHasil)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Fitur Live Formatting: Otomatis tambahkan titik ribuan saat diketik
        etBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val cleanString = s.toString().replace(".", "")
                if (cleanString.isNotEmpty()) {
                    val parsed = cleanString.toLongOrNull() ?: 0
                    val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
                        groupingSeparator = '.'
                    }
                    val formatter = DecimalFormat("#,###", symbols)
                    val formatted = formatter.format(parsed)

                    etBudget.setText(formatted)
                    etBudget.setSelection(formatted.length) // Menjaga posisi kursor di ujung
                }

                isFormatting = false
            }
        })

        // Muat data otomatis saat aplikasi dibuka kembali (Bahkan setelah Force Stop)
        muatDataTersimpan()

        // Tombol Simpan
        btnSimpan.setOnClickListener {
            val tujuan = etDestinasi.text.toString().trim()
            val budgetRaw = etBudget.text.toString().replace(".", "").trim()
            val durasi = etDurasi.text.toString().trim()
            val tahun = etTahun.text.toString().trim()

            if (tujuan.isEmpty() || budgetRaw.isEmpty()) {
                Toast.makeText(this, "⚠️ Kota tujuan dan budget wajib diisi!", Toast.LENGTH_SHORT).show()
            } else {
                val budgetDouble = budgetRaw.toDoubleOrNull() ?: 0.0

                // Simpan ke SharedPreferences (Kata sandi sengaja tidak disimpan)
                val pref = getSharedPreferences("DataTravelPro", Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("TUJUAN", tujuan)
                editor.putFloat("BUDGET", budgetDouble.toFloat())
                editor.putString("DURASI", durasi)
                editor.putString("TAHUN", tahun)
                editor.apply()

                tampilkanHasil(tujuan, budgetDouble, durasi, tahun)

                // Bersihkan kotak kata sandi demi keamanan
                etKataSandi.text.clear()

                Toast.makeText(this, "✅ Rencana berhasil disimpan secara permanen!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk memuat data lama dari memori SharedPreferences
    private fun muatDataTersimpan() {
        val pref = getSharedPreferences("DataTravelPro", Context.MODE_PRIVATE)
        val tujuan = pref.getString("TUJUAN", "")
        val budgetFloat = pref.getFloat("BUDGET", 0.0f)
        val durasi = pref.getString("DURASI", "")
        val tahun = pref.getString("TAHUN", "")

        if (!tujuan.isNullOrEmpty()) {
            etDestinasi.setText(tujuan)

            // Format angka budget dengan titik saat dimuat kembali
            val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply { groupingSeparator = '.' }
            val formatter = DecimalFormat("#,###", symbols)
            etBudget.setText(formatter.format(budgetFloat.toLong()))

            etDurasi.setText(durasi)
            etTahun.setText(tahun)

            tampilkanHasil(tujuan, budgetFloat.toDouble(), durasi ?: "0", tahun ?: "-")
        }
    }

    // Fungsi untuk menampilkan hasil format teks ke layar
    private fun tampilkanHasil(tujuan: String, budget: Double, durasi: String, tahun: String) {
        val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply { groupingSeparator = '.' }
        val formatter = DecimalFormat("#,###", symbols)
        val formattedBudget = formatter.format(budget)

        tvHasil.text = "🌍 DESTINASI IMPIAN:\n\n" +
                "📍 Tujuan : $tujuan\n" +
                "💵 Budget : Rp $formattedBudget\n" +
                "⏱️ Durasi : $durasi Hari\n" +
                "📅 Tahun  : $tahun\n" +
                "-----------------------------------\n" +
                "🔒 Sandi  : (Aman / Tidak Disimpan)\n" +
                "STATUS   : ✅ Tersimpan Aman (SharedPreferences)"
    }
}