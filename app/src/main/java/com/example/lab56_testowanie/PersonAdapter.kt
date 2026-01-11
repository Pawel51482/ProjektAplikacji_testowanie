package com.example.lab56_testowanie

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab56_testowanie.databinding.ItemPersonBinding

class PersonAdapter(
    private val items: List<Person>
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(val binding: ItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val p = items[position]
        holder.binding.tvName.text = "${p.firstName} ${p.lastName}"

        val ctx = holder.itemView.context
        fun valueOrNA(v: String): String =
            if (v.isBlank()) ctx.getString(R.string.value_not_provided) else v

        holder.binding.tvDetails.text = ctx.getString(
            R.string.person_details_template,
            valueOrNA(p.birthDate),
            valueOrNA(p.phone),
            valueOrNA(p.email),
            valueOrNA(p.address)
        )
    }

    override fun getItemCount(): Int = items.size
}
