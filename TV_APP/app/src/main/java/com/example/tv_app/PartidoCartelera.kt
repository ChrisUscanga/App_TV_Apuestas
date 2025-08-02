package com.example.tv_app

data class PartidoCartelera(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val marcador: String?, // Ejemplo: "2 - 1" o null si no ha jugado
    val estado: EstadoPartido,
    val fechaHora: String // Ejemplo: "Hoy, 20:00"
)

enum class EstadoPartido {
    EN_JUEGO, FINALIZADO, PROXIMO
} 