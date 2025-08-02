package com.example.tv_app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.tv_app.api.ApiClient
import com.example.tv_app.api.Event
import com.example.tv_app.api.Outcome
import com.example.tv_app.model.Bet
import com.example.tv_app.model.BetResult
import kotlinx.coroutines.launch
import kotlin.math.abs
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import com.bumptech.glide.Glide

class ApuestasActivity : FragmentActivity() {

    private lateinit var tvSaldo: TextView
    private lateinit var tvMatchTitle: TextView
    private lateinit var tvHomeTeam: TextView
    private lateinit var tvAwayTeam: TextView
    private lateinit var tvHomeOdds: TextView
    private lateinit var tvAwayOdds: TextView
    private lateinit var tvBetAmount: TextView
    private lateinit var btnDecreaseBet: Button
    private lateinit var btnIncreaseBet: Button
    private lateinit var btnWhoWon: Button
    private lateinit var btnPrevMatch: Button
    private lateinit var btnNextMatch: Button
    private lateinit var ivHomeLogo: ImageView
    private lateinit var ivAwayLogo: ImageView

    private var currentBalance = 1000.0
    private var currentBetAmount = 0.0
    private var currentMatchIndex = 0
    private var matches = listOf<Event>()
    private var currentBet: Bet? = null
    private var selectedTeam: String? = null
    private var selectedOdds = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apuestas)

        initializeViews()
        setupClickListeners()
        loadSoccerMatches()
    }

    private fun initializeViews() {
        tvSaldo = findViewById(R.id.tv_saldo)
        tvMatchTitle = findViewById(R.id.tv_match_title)
        tvHomeTeam = findViewById(R.id.tv_home_team)
        tvAwayTeam = findViewById(R.id.tv_away_team)
        tvHomeOdds = findViewById(R.id.tv_home_odds)
        tvAwayOdds = findViewById(R.id.tv_away_odds)
        tvBetAmount = findViewById(R.id.tv_bet_amount)
        btnDecreaseBet = findViewById(R.id.btn_decrease_bet)
        btnIncreaseBet = findViewById(R.id.btn_increase_bet)
        btnWhoWon = findViewById(R.id.btn_who_won)
        btnPrevMatch = findViewById(R.id.btn_prev_match)
        btnNextMatch = findViewById(R.id.btn_next_match)
        ivHomeLogo = findViewById(R.id.iv_home_logo)
        ivAwayLogo = findViewById(R.id.iv_away_logo)

        val btnMasSaldo = findViewById<Button>(R.id.btn_mas_saldo)
        btnMasSaldo.setOnClickListener {
            showAddCardDialog()
        }
    }

    private fun setupClickListeners() {
        btnDecreaseBet.setOnClickListener {
            if (currentBetAmount > 0) {
                currentBetAmount -= 10.0
                updateBetAmount()
            }
        }

        btnIncreaseBet.setOnClickListener {
            if (currentBetAmount < currentBalance) {
                currentBetAmount += 10.0
                updateBetAmount()
            }
        }

        btnWhoWon.setOnClickListener {
            if (currentBet != null) {
                simulateMatchResult()
            } else {
                Toast.makeText(this, "Primero debes hacer una apuesta", Toast.LENGTH_SHORT).show()
            }
        }

        btnPrevMatch.setOnClickListener {
            if (currentMatchIndex > 0) {
                currentMatchIndex--
                displayCurrentMatch()
            }
        }

        btnNextMatch.setOnClickListener {
            if (currentMatchIndex < matches.size - 1) {
                currentMatchIndex++
                displayCurrentMatch()
            }
        }

        tvHomeTeam.setOnClickListener {
            selectTeam(tvHomeTeam.text.toString(), selectedOdds)
        }

        tvAwayTeam.setOnClickListener {
            selectTeam(tvAwayTeam.text.toString(), selectedOdds)
        }
    }

    private fun loadSoccerMatches() {
        lifecycleScope.launch {
            try {
                // Obtener deportes disponibles
                val sports = ApiClient.oddsApiService.getSports(ApiClient.getApiKey())
                val soccerSport = sports.find { it.key.contains("soccer") }
                
                if (soccerSport != null) {
                    // Obtener partidos de fútbol
                    matches = ApiClient.oddsApiService.getOdds(
                        apiKey = ApiClient.getApiKey(),
                        regions = "us",
                        markets = "h2h",
                        oddsFormat = "american"
                    )
                    
                    if (matches.isNotEmpty()) {
                        displayCurrentMatch()
                    } else {
                        tvMatchTitle.text = "No hay partidos disponibles"
                    }
                } else {
                    tvMatchTitle.text = "No se encontraron deportes de fútbol"
                }
            } catch (e: Exception) {
                tvMatchTitle.text = "Error al cargar partidos: ${e.message}"
                // Usar datos de ejemplo si la API falla
                createSampleMatches()
            }
        }
    }

    private fun createSampleMatches() {
        // Crear partidos de ejemplo si la API no funciona
        matches = listOf(
            Event(
                id = "1",
                sport_key = "soccer",
                sport_title = "Soccer",
                commence_time = "2024-01-01T20:00:00Z",
                home_team = "Real Madrid",
                away_team = "Barcelona",
                bookmakers = listOf()
            ),
            Event(
                id = "2",
                sport_key = "soccer",
                sport_title = "Soccer",
                commence_time = "2024-01-01T22:00:00Z",
                home_team = "Manchester United",
                away_team = "Liverpool",
                bookmakers = listOf()
            ),
            Event(
                id = "3",
                sport_key = "soccer",
                sport_title = "Soccer",
                commence_time = "2024-01-02T20:00:00Z",
                home_team = "Bayern Munich",
                away_team = "Borussia Dortmund",
                bookmakers = listOf()
            )
        )
        displayCurrentMatch()
    }

    private fun displayCurrentMatch() {
        if (matches.isNotEmpty() && currentMatchIndex < matches.size) {
            val match = matches[currentMatchIndex]
            tvMatchTitle.text = "${match.home_team} vs ${match.away_team}"
            tvHomeTeam.text = match.home_team
            tvAwayTeam.text = match.away_team
            
            // Simular odds si no hay datos de la API
            val homeOdds = if (match.bookmakers.isNotEmpty()) {
                match.bookmakers.first().markets.first().outcomes.first().price
            } else {
                -150.0
            }
            
            val awayOdds = if (match.bookmakers.isNotEmpty()) {
                match.bookmakers.first().markets.first().outcomes.last().price
            } else {
                130.0
            }
            
            tvHomeOdds.text = "Odds: ${formatOdds(homeOdds)}"
            tvAwayOdds.text = "Odds: ${formatOdds(awayOdds)}"
            
            // Resetear apuesta actual
            currentBet = null
            selectedTeam = null
            currentBetAmount = 0.0
            updateBetAmount()

            // Resetear selección visual
            tvHomeTeam.setBackgroundResource(R.drawable.team_button_background)
            tvAwayTeam.setBackgroundResource(R.drawable.team_button_background)

            // Obtener y mostrar el escudo del equipo local
            getTeamLogoUrl(match.home_team) { url ->
                runOnUiThread {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.default_background)
                        .into(ivHomeLogo)
                }
            }

            // Obtener y mostrar el escudo del equipo visitante
            getTeamLogoUrl(match.away_team) { url ->
                runOnUiThread {
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.default_background)
                        .into(ivAwayLogo)
                }
            }
        }
    }

    private fun formatOdds(odds: Double): String {
        return if (odds > 0) "+${odds.toInt()}" else odds.toInt().toString()
    }

    private fun selectTeam(teamName: String, odds: Double) {
        selectedTeam = teamName
        selectedOdds = odds

        // Cambiar visualmente el equipo seleccionado
        if (tvHomeTeam.text.toString() == teamName) {
            tvHomeTeam.setBackgroundResource(R.drawable.team_button_selected)
            tvAwayTeam.setBackgroundResource(R.drawable.team_button_background)
        } else if (tvAwayTeam.text.toString() == teamName) {
            tvAwayTeam.setBackgroundResource(R.drawable.team_button_selected)
            tvHomeTeam.setBackgroundResource(R.drawable.team_button_background)
        }

        // Crear apuesta
        currentBet = Bet(
            eventId = matches[currentMatchIndex].id,
            homeTeam = matches[currentMatchIndex].home_team,
            awayTeam = matches[currentMatchIndex].away_team,
            selectedTeam = teamName,
            betAmount = currentBetAmount,
            odds = odds,
            potentialWinnings = calculateWinnings(currentBetAmount, odds)
        )
        
        Toast.makeText(this, "Apuesta seleccionada: $teamName", Toast.LENGTH_SHORT).show()
    }

    private fun calculateWinnings(betAmount: Double, odds: Double): Double {
        return if (odds > 0) {
            betAmount * (odds / 100.0)
        } else {
            betAmount * (100.0 / abs(odds))
        }
    }

    private fun updateBetAmount() {
        tvBetAmount.text = "Monto: $${currentBetAmount.toInt()}"
    }

    private fun simulateMatchResult() {
        val bet = currentBet ?: return
        
        // Simular resultado aleatorio
        val random = Math.random()
        val homeTeamWins = random < 0.4
        val awayTeamWins = random < 0.7 && random >= 0.4
        val draw = random >= 0.7
        
        val winner = when {
            homeTeamWins -> bet.homeTeam
            awayTeamWins -> bet.awayTeam
            else -> "Empate"
        }
        
        val userWon = bet.selectedTeam == winner
        
        // Actualizar saldo
        if (userWon) {
            val winnings = bet.betAmount + bet.potentialWinnings
            currentBalance += winnings
            showResultDialog("¡GANASTE!", "Tu equipo ganó! Ganas $${winnings.toInt()}", true)
        } else {
            currentBalance -= bet.betAmount
            showResultDialog("Perdiste", "Tu equipo perdió. Pierdes $${bet.betAmount.toInt()}", false)
        }
        
        updateSaldo()
        currentBet = null
        selectedTeam = null
        currentBetAmount = 0.0
        updateBetAmount()
    }

    private fun showResultDialog(title: String, message: String, isWin: Boolean) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateSaldo() {
        tvSaldo.text = "Saldo: $${currentBalance.toInt()}"
    }

    private fun showAddCardDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null)
        
        val etCardNumber = dialogView.findViewById<EditText>(R.id.et_card_number)
        val etCvv = dialogView.findViewById<EditText>(R.id.et_cvv)
        val etExpiryYear = dialogView.findViewById<EditText>(R.id.et_expiry_year)
        val etCardHolder = dialogView.findViewById<EditText>(R.id.et_card_holder)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnAddCard = dialogView.findViewById<Button>(R.id.btn_add_card)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnAddCard.setOnClickListener {
            val cardNumber = etCardNumber.text.toString().trim()
            val cvv = etCvv.text.toString().trim()
            val expiryYear = etExpiryYear.text.toString().trim()
            val cardHolder = etCardHolder.text.toString().trim()

            if (validateCardData(cardNumber, cvv, expiryYear, cardHolder)) {
                processCardAddition(cardNumber, cvv, expiryYear, cardHolder)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateCardData(cardNumber: String, cvv: String, expiryYear: String, cardHolder: String): Boolean {
        if (cardNumber.isEmpty()) {
            showError("Por favor ingresa el número de tarjeta")
            return false
        }
        if (cardNumber.length < 13 || cardNumber.length > 19) {
            showError("El número de tarjeta debe tener entre 13 y 19 dígitos")
            return false
        }
        if (cvv.isEmpty()) {
            showError("Por favor ingresa el CVV")
            return false
        }
        if (cvv.length < 3 || cvv.length > 4) {
            showError("El CVV debe tener 3 o 4 dígitos")
            return false
        }
        if (expiryYear.isEmpty()) {
            showError("Por favor ingresa el año de vencimiento")
            return false
        }
        if (expiryYear.length != 4) {
            showError("El año debe tener 4 dígitos")
            return false
        }
        if (cardHolder.isEmpty()) {
            showError("Por favor ingresa el nombre del propietario")
            return false
        }
        return true
    }

    private fun processCardAddition(cardNumber: String, cvv: String, expiryYear: String, cardHolder: String) {
        // Simular procesamiento
        AlertDialog.Builder(this)
            .setTitle("¡Éxito!")
            .setMessage("¡Listo! Tarjeta agregada correctamente.\n\nTarjeta: ****${cardNumber.takeLast(4)}\nPropietario: $cardHolder")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Tarjeta agregada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getTeamLogoUrl(teamName: String, callback: (String?) -> Unit) {
        Thread {
            try {
                val searchQuery = "equipo de futbol $teamName"
                val searchUrl = "https://es.wikipedia.org/w/api.php?action=query&list=search&srsearch=${URLEncoder.encode(searchQuery, "UTF-8")}&format=json"
                val searchResult = URL(searchUrl).readText()
                val searchJson = JSONObject(searchResult)
                val searchArray = searchJson.getJSONObject("query").getJSONArray("search")
                if (searchArray.length() == 0) {
                    callback(null)
                    return@Thread
                }
                val pageTitle = searchArray.getJSONObject(0).getString("title")
                val imageUrl = "https://es.wikipedia.org/w/api.php?action=query&titles=${URLEncoder.encode(pageTitle, "UTF-8")}&prop=pageimages&format=json&pithumbsize=500"
                val imageResult = URL(imageUrl).readText()
                val imageJson = JSONObject(imageResult)
                val pages = imageJson.getJSONObject("query").getJSONObject("pages")
                val pageId = pages.keys().next()
                val page = pages.getJSONObject(pageId)
                val thumb = page.optJSONObject("thumbnail")
                val logoUrl = thumb?.getString("source")
                callback(logoUrl)
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }
} 