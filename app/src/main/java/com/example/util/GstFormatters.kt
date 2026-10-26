package com.example.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object GstFormatters {

    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    fun formatInr(amount: Double): String {
        return try {
            indianCurrencyFormat.format(amount)
        } catch (_: Exception) {
            "₹" + DecimalFormat("#,##,##0.00").format(amount)
        }
    }

    /**
     * Converts a numeric amount to Indian Rupee Words
     * e.g., 35400.00 -> "Thirty-Five Thousand Four Hundred Rupees Only"
     */
    fun convertToWords(amount: Double): String {
        val totalPaise = Math.round(amount * 100).toLong()
        val rupees = totalPaise / 100
        val paise = totalPaise % 100

        val words = StringBuilder()
        if (rupees == 0L) {
            words.append("Zero Rupees")
        } else {
            words.append(convertNumber(rupees)).append(" Rupees")
        }

        if (paise > 0) {
            words.append(" and ").append(convertNumber(paise)).append(" Paise")
        }
        words.append(" Only")
        return words.toString().trim()
    }

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun convertNumber(n: Long): String {
        if (n < 0) return "Minus " + convertNumber(-n)
        if (n < 20) return units[n.toInt()]
        if (n < 100) return tens[(n / 10).toInt()] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
        if (n < 1000) return units[(n / 100).toInt()] + " Hundred" + (if (n % 100 != 0L) " and " + convertNumber(n % 100) else "")
        if (n < 100000) return convertNumber(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertNumber(n % 1000) else "")
        if (n < 10000000) return convertNumber(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertNumber(n % 100000) else "")
        return convertNumber(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertNumber(n % 10000000) else "")
    }
}
