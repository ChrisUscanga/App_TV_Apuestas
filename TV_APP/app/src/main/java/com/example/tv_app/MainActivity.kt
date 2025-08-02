package com.example.tv_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener el nombre del usuario del intent
        val username = intent.getStringExtra("USERNAME") ?: "Usuario"
        
        // Mostrar el nombre del usuario en el TextView de bienvenida
        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        welcomeText.text = "Bienvenido, $username"

        val carteleraRecyclerView = findViewById<RecyclerView>(R.id.cartelera_recycler_view)
        carteleraRecyclerView.layoutManager = LinearLayoutManager(this)

        // Lista de partidos de ejemplo
        val partidos = listOf(
            PartidoCartelera(
                id = "1",
                homeTeam = "Real Madrid",
                awayTeam = "Barcelona",
                marcador = "2 - 1",
                estado = EstadoPartido.FINALIZADO,
                fechaHora = "Ayer, 20:00"
            ),
            PartidoCartelera(
                id = "2",
                homeTeam = "Manchester United",
                awayTeam = "Liverpool",
                marcador = null,
                estado = EstadoPartido.EN_JUEGO,
                fechaHora = "Hoy, 18:00"
            ),
            PartidoCartelera(
                id = "3",
                homeTeam = "Bayern Munich",
                awayTeam = "Borussia Dortmund",
                marcador = null,
                estado = EstadoPartido.PROXIMO,
                fechaHora = "Mañana, 21:00"
            )
        )

        val adapter = PartidosCarteleraAdapter(partidos) { partido ->
            val intent = Intent(this, ApuestasActivity::class.java)
            intent.putExtra("HOME_TEAM", partido.homeTeam)
            intent.putExtra("AWAY_TEAM", partido.awayTeam)
            intent.putExtra("PARTIDO_ID", partido.id)
            startActivity(intent)
        }
        carteleraRecyclerView.adapter = adapter
    }
}