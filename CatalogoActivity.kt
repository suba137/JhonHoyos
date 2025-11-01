package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatalogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCatalogo)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = InmuebleAdapter(FortivaApp.listaInmuebles) { inmueble ->
            val intent = Intent(this, CompraParcialActivity::class.java)
            intent.putExtra("idInmueble", inmueble.id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<RecyclerView>(R.id.recyclerViewCatalogo).adapter?.notifyDataSetChanged()
    }
}
