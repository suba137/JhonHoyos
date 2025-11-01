package com.example.fortiva

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InmuebleAdapter(
    private val lista: List<Inmueble>,
    private val onComprarClick: (Inmueble) -> Unit
) : RecyclerView.Adapter<InmuebleAdapter.InmuebleViewHolder>() {

    inner class InmuebleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarInmueble)
        val tvDisponible: TextView = view.findViewById(R.id.tvDisponible)
        val btnComprar: Button = view.findViewById(R.id.btnComprar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InmuebleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inmueble, parent, false)
        return InmuebleViewHolder(view)
    }

    override fun onBindViewHolder(holder: InmuebleViewHolder, position: Int) {
        val inmueble = lista[position]

        holder.tvNombre.text = inmueble.nombre
        holder.tvPrecio.text = "Precio: $${String.format("%,.0f", inmueble.valorTotal)}"
        holder.progressBar.progress = inmueble.disponible.toInt()
        holder.tvDisponible.text = "Disponible: ${String.format("%.0f", inmueble.disponible)}%"

        holder.btnComprar.setOnClickListener { onComprarClick(inmueble) }
    }

    override fun getItemCount(): Int = lista.size
}
