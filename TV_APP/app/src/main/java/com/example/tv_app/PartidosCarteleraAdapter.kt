package com.example.tv_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PartidosCarteleraAdapter(
    private val partidos: List<PartidoCartelera>,
    private val onApostarClick: (PartidoCartelera) -> Unit
) : RecyclerView.Adapter<PartidosCarteleraAdapter.PartidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido_cartelera, parent, false)
        return PartidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        val partido = partidos[position]
        holder.bind(partido)
        holder.btnApostar.setOnClickListener { onApostarClick(partido) }
    }

    override fun getItemCount() = partidos.size

    class PartidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEstado: TextView = view.findViewById(R.id.tv_estado_partido)
        private val tvHome: TextView = view.findViewById(R.id.tv_home_team_cartelera)
        private val tvAway: TextView = view.findViewById(R.id.tv_away_team_cartelera)
        private val tvMarcador: TextView = view.findViewById(R.id.tv_marcador)
        private val tvFechaHora: TextView = view.findViewById(R.id.tv_fecha_hora)
        val btnApostar: Button = view.findViewById(R.id.btn_apostar)

        fun bind(partido: PartidoCartelera) {
            tvHome.text = partido.homeTeam
            tvAway.text = partido.awayTeam
            tvFechaHora.text = partido.fechaHora
            tvMarcador.text = partido.marcador ?: "-"
            when (partido.estado) {
                EstadoPartido.EN_JUEGO -> {
                    tvEstado.text = "EN JUEGO"
                    tvEstado.setTextColor(0xFF43A047.toInt())
                    btnApostar.visibility = View.VISIBLE
                }
                EstadoPartido.FINALIZADO -> {
                    tvEstado.text = "FINALIZADO"
                    tvEstado.setTextColor(0xFFB71C1C.toInt())
                    btnApostar.visibility = View.GONE
                }
                EstadoPartido.PROXIMO -> {
                    tvEstado.text = "PRÓXIMO"
                    tvEstado.setTextColor(0xFF1976D2.toInt())
                    btnApostar.visibility = View.VISIBLE
                }
            }
        }
    }
} 