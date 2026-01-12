package com.cebolao.lotofacil.util

import android.content.Context
import android.content.Intent
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.GameComputedMetrics

/**
 * Helper object to handle sharing of Lotofacil games.
 */
object GameSharer {
    
    fun shareGame(context: Context, numbers: List<Int>, metrics: GameComputedMetrics) {
        val numbersFormatted = numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val text = context.getString(
            R.string.share_game_text_format,
            numbersFormatted,
            metrics.sum, metrics.evens, metrics.primes, metrics.frame, metrics.center,
            metrics.fibonacci, metrics.multiplesOf3, metrics.repeated, metrics.sequences
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.games_share_chooser_title)
            )
        )
    }
}
