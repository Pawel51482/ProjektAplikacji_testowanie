package com.example.lab56_testowanie

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab56_testowanie.databinding.ActivityDeletePersonBinding

class DeletePersonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeletePersonBinding
    private lateinit var dbHelper: PersonDbHelper
    private lateinit var adapter: PersonDeleteAdapter

    private val allPersons: MutableList<Person> = mutableListOf()

    private var searchWatcher: TextWatcher? = null
    private var confirmDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeletePersonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_delete_person)

        dbHelper = PersonDbHelper(applicationContext)

        allPersons.addAll(dbHelper.getAllPersons())

        adapter = PersonDeleteAdapter(allPersons.toMutableList()) { person ->
            showDeleteDialog(person)
        }

        binding.rvDeletePersons.layoutManager = LinearLayoutManager(this)
        binding.rvDeletePersons.adapter = adapter

        searchWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                filterList(query)
            }
        }
        binding.etSearch.addTextChangedListener(searchWatcher)
    }

    private fun filterList(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allPersons)
            return
        }

        val lower = query.lowercase()
        val filtered = allPersons.filter { person ->
            person.firstName.lowercase().contains(lower) ||
                    person.lastName.lowercase().contains(lower) ||
                    person.phone.lowercase().contains(lower) ||
                    person.email.lowercase().contains(lower) ||
                    person.address.lowercase().contains(lower) ||
                    person.birthDate.lowercase().contains(lower)
        }

        adapter.updateData(filtered)
    }

    private fun showDeleteDialog(person: Person) {
        confirmDialog?.dismiss()
        confirmDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message, person.firstName, person.lastName))
            .setPositiveButton(getString(R.string.dialog_delete_positive)) { _, _ ->
                deletePerson(person)
            }
            .setNegativeButton(getString(R.string.dialog_delete_negative), null)
            .create()

        confirmDialog?.show()
    }

    private fun deletePerson(person: Person) {
        val rows = dbHelper.deletePersonById(person.id)
        if (rows > 0) {
            Toast.makeText(
                this,
                getString(R.string.toast_person_deleted, person.firstName, person.lastName),
                Toast.LENGTH_SHORT
            ).show()

            allPersons.removeAll { it.id == person.id }

            val currentQuery = binding.etSearch.text.toString().trim()
            filterList(currentQuery)
        } else {
            Toast.makeText(this, getString(R.string.toast_delete_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        searchWatcher?.let { binding.etSearch.removeTextChangedListener(it) }
        searchWatcher = null

        confirmDialog?.dismiss()
        confirmDialog = null

        binding.rvDeletePersons.adapter = null

        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
