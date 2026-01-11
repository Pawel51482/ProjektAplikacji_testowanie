package com.example.lab56_testowanie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lab56_testowanie.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isSwitchingLanguage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applySavedLanguage(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }

        binding.btnList.setOnClickListener {
            startActivity(Intent(this, ListPersonsActivity::class.java))
        }

        binding.btnDelete.setOnClickListener {
            startActivity(Intent(this, DeletePersonActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        (binding.btnLanguage as? MaterialButton)?.setIconResource(R.drawable.ic_language)

        updateLanguageButtonText()

        binding.btnLanguage.setOnClickListener {
            if (isSwitchingLanguage) return@setOnClickListener
            isSwitchingLanguage = true
            binding.btnLanguage.isEnabled = false

            LanguageManager.toggleLanguage(this)
            binding.btnLanguage.post { recreate() }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    startActivity(Intent(this, AddPersonActivity::class.java))
                    true
                }
                R.id.nav_list -> {
                    startActivity(Intent(this, ListPersonsActivity::class.java))
                    true
                }
                R.id.nav_delete -> {
                    startActivity(Intent(this, DeletePersonActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Po odświeżeniu aktywności przycisk znów ma działać
        isSwitchingLanguage = false
        binding.btnLanguage.isEnabled = true
        updateLanguageButtonText()
    }

    private fun updateLanguageButtonText() {
        val current = LanguageManager.getCurrentLanguage(this)
        binding.btnLanguage.text = if (current == "pl") {
            getString(R.string.btn_language_to_en)
        } else {
            getString(R.string.btn_language_to_pl)
        }
    }
}
