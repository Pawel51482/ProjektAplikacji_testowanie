package com.example.lab56_testowanie

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lab56_testowanie.databinding.ActivityAddPersonBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun String.capitalizeFirstLetter(): String {
    if (this.isEmpty()) return this
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

class AddPersonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPersonBinding
    private lateinit var dbHelper: PersonDbHelper

    private var isUpdatingPhone = false
    private var phoneWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // jeśli używasz przełączania języka, dobrze to mieć:
        LanguageManager.applySavedLanguage(this)

        super.onCreate(savedInstanceState)
        binding = ActivityAddPersonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_add_person)

        dbHelper = PersonDbHelper(applicationContext)

        phoneWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingPhone) return
                val digits = s?.toString().orEmpty().filter { it.isDigit() }.take(9)
                val formatted = buildString {
                    digits.forEachIndexed { index, c ->
                        append(c)
                        if ((index == 2 || index == 5) && index != digits.lastIndex) append(' ')
                    }
                }
                isUpdatingPhone = true
                binding.etPhone.setText(formatted)
                binding.etPhone.setSelection(formatted.length)
                isUpdatingPhone = false
            }
        }
        binding.etPhone.addTextChangedListener(phoneWatcher)

        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { savePerson() }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->
                val dd = d.toString().padStart(2, '0')
                val mm = (m + 1).toString().padStart(2, '0')
                binding.etBirthDate.setText("$dd-$mm-$y")
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )

        // blokada przyszłej daty
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun validateBirthDateRequired(value: String): Pair<Boolean, String?> {
        if (value.isBlank()) return false to getString(R.string.error_birth_date_invalid)

        val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        fmt.isLenient = false

        val parsed = try {
            fmt.parse(value)
        } catch (_: Exception) {
            return false to getString(R.string.error_birth_date_invalid)
        } ?: return false to getString(R.string.error_birth_date_invalid)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return if (parsed.after(today)) {
            false to getString(R.string.error_birth_date_future)
        } else {
            true to null
        }
    }

    private fun savePerson() {
        val firstName = binding.etFirstName.text.toString().trim().capitalizeFirstLetter()
        val lastName = binding.etLastName.text.toString().trim().capitalizeFirstLetter()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val phoneInput = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        val city = binding.etCity.text.toString().trim().capitalizeFirstLetter()
        val postalCode = binding.etPostalCode.text.toString().trim()
        val street = binding.etStreet.text.toString().trim().capitalizeFirstLetter()
        val houseNumber = binding.etHouseNumber.text.toString().trim()
        val apartmentNumber = binding.etApartmentNumber.text.toString().trim()

        // Czyścimy stare błędy
        binding.etFirstName.error = null
        binding.etLastName.error = null
        binding.etBirthDate.error = null
        binding.etPhone.error = null
        binding.etEmail.error = null
        binding.etCity.error = null
        binding.etPostalCode.error = null
        binding.etStreet.error = null
        binding.etHouseNumber.error = null
        binding.etApartmentNumber.error = null

        var isValid = true

        // Wymagane pola
        if (firstName.isEmpty()) {
            binding.etFirstName.error = getString(R.string.error_first_name_required)
            isValid = false
        }
        if (lastName.isEmpty()) {
            binding.etLastName.error = getString(R.string.error_last_name_required)
            isValid = false
        }

        val (birthOk, birthErr) = validateBirthDateRequired(birthDate)
        if (!birthOk) {
            binding.etBirthDate.error = birthErr
            isValid = false
        }

        // Telefon
        val digitsOnly = phoneInput.replace(" ", "")
        if (digitsOnly.length != 9 || !digitsOnly.all { it.isDigit() }) {
            binding.etPhone.error = getString(R.string.error_phone_invalid)
            isValid = false
        }

        // email (wymagany i poprawny)
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            isValid = false
        }

        // Adres
        val postalRegex = Regex("\\d{2}-\\d{3}")
        val numberRegex = Regex("\\d+[A-Za-z]?")

        if (city.isEmpty()) {
            binding.etCity.error = getString(R.string.error_city_required)
            isValid = false
        }
        if (!postalRegex.matches(postalCode)) {
            binding.etPostalCode.error = getString(R.string.error_postal_invalid)
            isValid = false
        }
        if (street.isEmpty()) {
            binding.etStreet.error = getString(R.string.error_street_required)
            isValid = false
        }
        if (!numberRegex.matches(houseNumber)) {
            binding.etHouseNumber.error = getString(R.string.error_house_number_invalid)
            isValid = false
        }

        // numer lokalu (opcjonalny)
        if (apartmentNumber.isNotEmpty() && !numberRegex.matches(apartmentNumber)) {
            binding.etApartmentNumber.error = getString(R.string.error_house_number_invalid)
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.toast_fix_errors), Toast.LENGTH_SHORT).show()
            return
        }

        val formattedPhone = "${digitsOnly.substring(0, 3)} ${digitsOnly.substring(3, 6)} ${digitsOnly.substring(6, 9)}"
        val flat = if (apartmentNumber.isBlank()) "" else "/$apartmentNumber"
        val fullAddress = "$street $houseNumber$flat, $postalCode $city"

        val person = Person(
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            phone = formattedPhone,
            email = email,
            address = fullAddress
        )

        val id = dbHelper.insertPerson(person)
        if (id > 0) {
            Toast.makeText(
                this,
                getString(R.string.toast_person_added, firstName, lastName),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.toast_save_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        phoneWatcher?.let { binding.etPhone.removeTextChangedListener(it) }
        phoneWatcher = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
