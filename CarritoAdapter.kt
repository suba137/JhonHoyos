package com.example.fortiva

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class CarritoAdapter(
    private var listaCompras: MutableList<CompraParcial>,   // 🔁 MutableList para actualizar
    private val onEliminar: (CompraParcial) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreInmueble)
        val tvPorcentaje: TextView = itemView.findViewById(R.id.tvPorcentajeCompra)
        val tvValor: TextView = itemView.findViewById(R.id.tvValorCompra)
        val tvRestante: TextView = itemView.findViewById(R.id.tvValorRestante)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val compra = listaCompras[position]

        val nf = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        holder.tvNombre.text = compra.inmueble.nombre
        holder.tvPorcentaje.text = "Porcentaje: ${compra.porcentajeCompra.toInt()}%"
        holder.tvValor.text = "Valor: ${nf.format(compra.valorCompra)}"
        holder.tvRestante.text = "Restante: ${nf.format(compra.valorRestante)}"

        holder.btnEliminar.setOnClickListener {
            onEliminar(compra)
        }
    }

    override fun getItemCount(): Int = listaCompras.size

    // Método para actualizar la lista visualmente tras cambios en el ViewModel
    fun actualizarLista(nuevaLista: MutableList<CompraParcial>) {
        listaCompras = nuevaLista
        notifyDataSetChanged()
    }
}
